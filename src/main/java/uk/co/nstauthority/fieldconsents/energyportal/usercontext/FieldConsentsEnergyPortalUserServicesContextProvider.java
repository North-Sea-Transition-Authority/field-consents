package uk.co.nstauthority.fieldconsents.energyportal.usercontext;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.starter.usercontext.EnergyPortalUserServicesContextProvider;
import uk.co.fivium.energyportal.starter.usercontext.UserContextV1;
import uk.co.fivium.energyportal.starter.usercontext.VersionedUserContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Component
class FieldConsentsEnergyPortalUserServicesContextProvider implements EnergyPortalUserServicesContextProvider {

  private final EnergyPortalUserService energyPortalUserService;
  private final TeamQueryService teamQueryService;
  private final ApplicationVersionService applicationVersionService;
  private final TechnicalReviewService technicalReviewService;
  private final ConsultationService consultationService;
  private final OrganisationGroupQueryService organisationGroupQueryService;
  private final ApplicationUpdateService applicationUpdateService;
  private final Clock clock;

  FieldConsentsEnergyPortalUserServicesContextProvider(
      EnergyPortalUserService energyPortalUserService,
      TeamQueryService teamQueryService,
      ApplicationVersionService applicationVersionService,
      TechnicalReviewService technicalReviewService,
      ConsultationService consultationService,
      OrganisationGroupQueryService organisationGroupQueryService,
      ApplicationUpdateService applicationUpdateService,
      Clock clock
  ) {
    this.energyPortalUserService = energyPortalUserService;
    this.teamQueryService = teamQueryService;
    this.applicationVersionService = applicationVersionService;
    this.technicalReviewService = technicalReviewService;
    this.consultationService = consultationService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.applicationUpdateService = applicationUpdateService;
    this.clock = clock;
  }

  @Override
  public VersionedUserContext getUserContext(long wuaId) {
    var serviceUserDetail = energyPortalUserService.getServiceUserByWuaId(WebUserAccountId.from(wuaId));
    var userContextBuilder = VersionedUserContext.newBuilder().v1();
    var userRoleContext = UserRoleContext.from(teamQueryService.getTeamRoles(serviceUserDetail));
    var now = Instant.now(clock);

    if (userRoleContext.hasAnyRole(TeamType.INDUSTRY, RoleGroup.INDUSTRY_PAY_AND_SUBMIT_APPLICATION_ROLES)) {
      var organisationGroupIds = userRoleContext
          .scopeIdsForAnyIndustryRole(RoleGroup.INDUSTRY_PAY_AND_SUBMIT_APPLICATION_ROLES)
          .stream()
          .map(Integer::parseInt)
          .toList();

      var organisationUnitIds = organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(organisationGroupIds)
          .stream()
          .map(OrganisationUnitJson::organisationUnitId)
          .toList();

      if (!organisationUnitIds.isEmpty()) {
        var numApplicationsAwaitingPayment = applicationVersionService.countByPrimaryOperatorInAndStatus(
              organisationUnitIds,
              ApplicationVersionStatus.AWAITING_PAYMENT
        );

        addApplicationsAwaitingPayment(numApplicationsAwaitingPayment, userContextBuilder);

        var numUpdatesByOverdue = applicationUpdateService.getUpdatesByStatusAndPrimaryOperatorIds(
              ApplicationUpdateStatus.OPEN,
              organisationUnitIds
            ).stream()
            .filter(update -> update.getDeadlineDateTime() != null)
            .collect(Collectors.partitioningBy(update -> !update.getDeadlineDateTime().isAfter(now), Collectors.counting()));

        addUpdatesRequestedAndOverdue(numUpdatesByOverdue.get(false), numUpdatesByOverdue.get(true), userContextBuilder);
      }
    }

    if (userRoleContext.hasRole(TeamType.REGULATOR, Role.CASE_MANAGER)) {
      var numApplicationsWithoutCaseOfficer = applicationVersionService.countLatestSubmittedVersionsWithoutCaseOfficer();

      addApplicationsUnassignedToCaseOfficer(numApplicationsWithoutCaseOfficer, userContextBuilder);
    }

    if (userRoleContext.hasRole(TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)) {
      var numTechReviewsByOverdue = technicalReviewService.findTechnicalReviewsByReviewerAndStatus(
            serviceUserDetail,
            TechnicalReviewStatus.OPEN
          ).stream()
          .filter(technicalReview -> {
            var deadline = technicalReview.getDeadlineDateTime();
            return deadline != null && !deadline.isAfter(now.plus(24, ChronoUnit.HOURS));
          })
          .collect(Collectors.partitioningBy(technicalReview -> !technicalReview.getDeadlineDateTime().isAfter(now),
              Collectors.counting()));

      addTechnicalReviewsDueSoonAndOverdue(
          numTechReviewsByOverdue.get(false),
          numTechReviewsByOverdue.get(true),
          userContextBuilder
      );
    }

    if (userRoleContext.hasAnyRole(TeamType.CONSULTEE, RoleGroup.CONSULTEE_VIEW_CASE_PROCESSING_ROLES)) {
      var openConsultations = consultationService.findAllOpenConsultations();

      if (userRoleContext.hasRole(TeamType.CONSULTEE, Role.ALLOCATOR)) {
        var numUnAllocatedConsultations = openConsultations
            .stream()
            .filter(consultation -> consultation.getResponderWuaId() == null)
            .count();

        addUnallocatedConsultations(numUnAllocatedConsultations, userContextBuilder);
      }

      if (userRoleContext.hasRole(TeamType.CONSULTEE, Role.RESPONDER)) {
        var numConsultationsByOverdue = openConsultations.stream()
            .filter(consultation -> Objects.equals(consultation.getResponderWuaId(), serviceUserDetail.wuaId()))
            .filter(consultation -> {
              var deadline = consultation.getRequestDeadline();
              return deadline != null && !deadline.isAfter(now.plus(24, ChronoUnit.HOURS));
            })
            .collect(Collectors.partitioningBy(consultation ->
                    !consultation.getRequestDeadline().isAfter(now),
                Collectors.counting())
            );

        addConsultationsDueSoonAndOverdue(
            numConsultationsByOverdue.get(false),
            numConsultationsByOverdue.get(true),
            userContextBuilder
        );
      }
    }

    return userContextBuilder.build();
  }

  private void addApplicationsAwaitingPayment(long numApplicationsAwaitingPayment, UserContextV1.Builder userContextBuilder) {
    if (numApplicationsAwaitingPayment > 0) {
      userContextBuilder.low(
          Math.toIntExact(numApplicationsAwaitingPayment),
          "application%s awaiting payment".formatted(numApplicationsAwaitingPayment == 1 ? "" : "s")
      );
    }
  }

  private void addUpdatesRequestedAndOverdue(
      long numUpdatesRequested,
      long numUpdatesOverdue,
      UserContextV1.Builder userContextBuilder
  ) {
    if (numUpdatesRequested > 0) {
      userContextBuilder.low(
          Math.toIntExact(numUpdatesRequested),
          "update%s requested".formatted(numUpdatesRequested == 1 ? "" : "s")
      );
    }

    if (numUpdatesOverdue > 0) {
      userContextBuilder.high(
          Math.toIntExact(numUpdatesOverdue),
          "update%s overdue".formatted(numUpdatesOverdue == 1 ? "" : "s")
      );
    }
  }

  private void  addApplicationsUnassignedToCaseOfficer(
      long numApplicationsWithoutCaseOfficer,
      UserContextV1.Builder userContextBuilder
  ) {
    if (numApplicationsWithoutCaseOfficer > 0) {
      userContextBuilder.low(
          Math.toIntExact(numApplicationsWithoutCaseOfficer),
          "application%s awaiting assignment".formatted(numApplicationsWithoutCaseOfficer == 1 ? "" : "s")
      );
    }
  }

  private void addTechnicalReviewsDueSoonAndOverdue(
      long numTechReviewsDueSoon,
      long numTechReviewsOverdue,
      UserContextV1.Builder userContextBuilder
  ) {
    if (numTechReviewsDueSoon > 0) {
      userContextBuilder.low(
          Math.toIntExact(numTechReviewsDueSoon),
          "review%s due within 24 hours".formatted(numTechReviewsDueSoon == 1 ? "" : "s")
      );
    }

    if (numTechReviewsOverdue > 0) {
      userContextBuilder.high(
          Math.toIntExact(numTechReviewsOverdue),
          "review%s overdue".formatted(numTechReviewsOverdue == 1 ? "" : "s")
      );
    }
  }

  private void addUnallocatedConsultations(long numUnAllocatedConsultations, UserContextV1.Builder userContextBuilder) {
    if (numUnAllocatedConsultations > 0) {
      userContextBuilder.low(
          Math.toIntExact(numUnAllocatedConsultations),
          "consultation%s awaiting assignment".formatted(numUnAllocatedConsultations == 1 ? "" : "s")
      );
    }
  }

  private void addConsultationsDueSoonAndOverdue(
      long numConsultationsDueSoon,
      long numConsultationsOverdue,
      UserContextV1.Builder userContextBuilder
  ) {
    if (numConsultationsDueSoon > 0) {
      userContextBuilder.low(
          Math.toIntExact(numConsultationsDueSoon),
          "consultation%s due within 24 hours".formatted(numConsultationsDueSoon == 1 ? "" : "s")
      );
    }

    if (numConsultationsOverdue > 0) {
      userContextBuilder.high(
          Math.toIntExact(numConsultationsOverdue),
          "consultation%s overdue".formatted(numConsultationsOverdue == 1 ? "" : "s")
      );
    }
  }
}

package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentEmailService.ORGANISATION_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartner;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentEmailService;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Service
class BulkIssueConsentEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BulkIssueConsentEmailService.class);
  static final String SUCCESSFUL_APPLICATIONS_MERGE_FIELD_TEXT =
      "The following consents have been granted and issued:" + System.lineSeparator() + "%s";
  static final String FAILED_APPLICATIONS_MERGE_FIELD_TEXT =
      "The following consents failed to be issued:" + System.lineSeparator() + "%s";
  static final String SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT =
      "The following consents have been granted and issued for a field " +
          "which your organisation is an equity partner:" + System.lineSeparator() + "%s";
  static final String SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME = "SUCCESSFUL_APPLICATIONS";
  static final String FAILED_APPLICATIONS_MERGE_FIELD_NAME = "FAILED_APPLICATIONS";
  static final String WORK_AREA_URL_MERGE_FIELD_NAME = "WORK_AREA_URL";
  static final String SUBJECT_TEXT_MERGE_FIELD_NAME = "SUBJECT_TEXT";

  private final EmailService emailService;
  private final AbsoluteUrlService absoluteUrlService;
  private final EnergyPortalUserService energyPortalUserService;
  private final ApplicationService applicationService;
  private final OrganisationUnitService organisationUnitService;
  private final TeamMemberViewService teamMemberViewService;
  private final IndustryTeamService industryTeamService;
  private final ConsentFieldEquityPartnerService consentFieldEquityPartnerService;
  private final ConsentEmailService consentEmailService;

  BulkIssueConsentEmailService(
      EmailService emailService,
      AbsoluteUrlService absoluteUrlService,
      EnergyPortalUserService energyPortalUserService,
      ApplicationService applicationService,
      OrganisationUnitService organisationUnitService,
      TeamMemberViewService teamMemberViewService,
      IndustryTeamService industryTeamService,
      ConsentFieldEquityPartnerService consentFieldEquityPartnerService,
      ConsentEmailService consentEmailService
  ) {
    this.emailService = emailService;
    this.absoluteUrlService = absoluteUrlService;
    this.energyPortalUserService = energyPortalUserService;
    this.applicationService = applicationService;
    this.organisationUnitService = organisationUnitService;
    this.teamMemberViewService = teamMemberViewService;
    this.industryTeamService = industryTeamService;
    this.consentFieldEquityPartnerService = consentFieldEquityPartnerService;
    this.consentEmailService = consentEmailService;
  }

  void sendBulkConsentIssuedEmailToRegulators(BulkIssueConsentRun run, List<BulkIssueConsentsTask> tasks) {
    var tasksByCaseOfficerWuaId = tasks.stream().collect(groupingBy(task -> task.getApplicationVersion().getCaseOfficerWuaId()));
    var tasksByCamUserWuaId = tasks.stream().collect(groupingBy(task -> task.getApplicationVersion().getCamWuaId()));

    // When a case officer is also in the CAM role, and they have both processed and granted the consent
    // we're going to have only one user in the resulting map with all the tasks they're involved in as a case officer or CAM
    var tasksByRegulatorUserWuaId = Stream
        .concat(tasksByCaseOfficerWuaId.entrySet().stream(), tasksByCamUserWuaId.entrySet().stream())
        .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toSet())));

    for (var entry : tasksByRegulatorUserWuaId.entrySet()) {
      sendBulkConsentIssuedEmailToRegulator(run, WebUserAccountId.from(entry.getKey()), entry.getValue());
    }
  }

  void sendBulkConsentIssuedEmailToRegulator(
      BulkIssueConsentRun run,
      WebUserAccountId userWuaId,
      Collection<BulkIssueConsentsTask> tasks
  ) {
    var successfulApplications = getSuccessfulApplications(tasks);
    var failedApplications = getFailedApplications(tasks);

    var recipient = FieldConsentsEmailRecipient.from(energyPortalUserService.getByWuaId(userWuaId));

    try {
      sendEmailToRegulator(
          run,
          recipient,
          formatStringList(successfulApplications),
          formatStringList(failedApplications));
    } catch (Exception exception) {
      LOGGER.error("""
            An attempt to send a bulk consents issued notification to regulator \
            by user with wuaId [{}] for bulk issue consent run with id [{}] failed. \
            Note: this hasn't prevented the bulk consents being issued.
            """,
          run.getIssuedByWuaId(), run.getId(), exception);
    }
  }

  void sendEmailToRegulator(
      BulkIssueConsentRun run,
      FieldConsentsEmailRecipient recipient,
      String formattedSuccessfulApplications,
      String formattedFailedApplications
  ) {
    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_REGULATOR)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, recipient.displayName())
        .withMailMergeField(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued")
        .withMailMergeField(WORK_AREA_URL_MERGE_FIELD_NAME,
            absoluteUrlService.getAbsoluteUrl(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .withMailMergeField(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
            formattedSuccessfulApplications.isEmpty()
                ? ""
                : String.format(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_TEXT, formattedSuccessfulApplications))
        .withMailMergeField(FAILED_APPLICATIONS_MERGE_FIELD_NAME,
            formattedFailedApplications.isEmpty()
                ? ""
                : String.format(FAILED_APPLICATIONS_MERGE_FIELD_TEXT, formattedFailedApplications))
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        recipient,
        run
    );
  }

  void sendBulkConsentIssuedEmailToOperators(BulkIssueConsentRun run, List<BulkIssueConsentsTask> tasks) {
    Map<Long, List<BulkIssueConsentsTask>> tasksBySubmitterWuaId = tasks.stream()
        .collect(groupingBy(task -> task.getApplicationVersion().getSubmittedByWuaId()));

    Map<Integer, List<BulkIssueConsentsTask>> tasksByOperatorId = tasks.stream()
        .collect(groupingBy(task -> task.getApplicationVersion().getPrimaryOperatorOuId()));

    Map<Long, List<BulkIssueConsentsTask>> tasksByConsentRecipientWuaId = new HashMap<>();

    tasksByOperatorId.forEach((operatorId, bulkTasks) -> {
      var primaryOperator = organisationUnitService.getOrganisationUnitWithGroupsById(
          operatorId,
          ORGANISATION_LOOKUP_PURPOSE);

      var consentRecipientWuaIds = new HashSet<Long>();

      // get the consent recipient wua ids for the organisation associated with the current operator
      primaryOperator.organisationGroups().forEach(organisationGroupDto ->
          consentRecipientWuaIds.addAll(getConsentRecipientWuaIds(organisationGroupDto)));

      // on each iteration add the new tasks for the current operator id. The operator might correspond to a new org group
      // or to an org group already encountered. In the latter case we simply concat the new tasks to the current list of
      // tasks for that recipient
      consentRecipientWuaIds.forEach(wuaId -> {
        var currentTasksForRecipient = tasksByConsentRecipientWuaId.get(wuaId);
        if (currentTasksForRecipient == null) {
          tasksByConsentRecipientWuaId.put(wuaId, bulkTasks);
        } else {
          tasksByConsentRecipientWuaId.put(wuaId,
              Stream.concat(currentTasksForRecipient.stream(), bulkTasks.stream()).toList());
        }
      });

    });

    // When a submitter is also in the consent recipient role, we're going to have only one user
    // in the resulting map with all the tasks they're involved in as a submitter or consent recipient
    var tasksByOperatorWuaId = Stream
        .concat(tasksBySubmitterWuaId.entrySet().stream(), tasksByConsentRecipientWuaId.entrySet().stream())
        .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toSet())));

    for (var entry : tasksByOperatorWuaId.entrySet()) {
      sendBulkConsentIssuedEmailToOperator(run, WebUserAccountId.from(entry.getKey()), entry.getValue());
    }
  }

  void sendBulkConsentIssuedEmailToOperator(
      BulkIssueConsentRun run,
      WebUserAccountId userWuaId,
      Collection<BulkIssueConsentsTask> tasks
  ) {
    var successfulApplications = getSuccessfulApplications(tasks);
    var recipient = FieldConsentsEmailRecipient.from(energyPortalUserService.getByWuaId(userWuaId));
    var formattedSuccessfulApplications = formatStringList(successfulApplications);

    var mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_OPERATOR)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, recipient.displayName())
        .withMailMergeField(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued")
        .withMailMergeField(WORK_AREA_URL_MERGE_FIELD_NAME,
            absoluteUrlService.getAbsoluteUrl(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
        .withMailMergeField(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
            formattedSuccessfulApplications.isEmpty()
                ? ""
                : String.format(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_TEXT, formattedSuccessfulApplications))
        .merge();
    try {
      emailService.sendEmail(mergedTemplate, recipient, run);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a bulk consents issued notification to the operator \
              by user with wuaId [{}] for bulk issue consent run with id [{}] failed. \
              Note: this hasn't prevented the bulk consents being issued.
              """,
          run.getIssuedByWuaId(), run.getId(), exception);
    }
  }

  Set<Long> getConsentRecipientWuaIds(OrganisationGroupDto organisationGroupDto) {
    var teamOptional = industryTeamService.getTeamByOrganisationGroupId(organisationGroupDto.getOrganisationGroupId());
    var consentRecipientWuaIds = new HashSet<Long>();

    if (teamOptional.isPresent()) {
      var teamConsentRecipients = teamMemberViewService
          .getTeamMemberViewsWithRolesForTeam(
              teamOptional.get(),
              Set.of(IndustryTeamRole.CONSENT_RECIPIENT))
          .stream()
          .map(tmv -> tmv.wuaId().id())
          .toList();

      consentRecipientWuaIds.addAll(teamConsentRecipients);
    }

    return consentRecipientWuaIds;
  }

  public void sendBulkConsentIssuedEmailToFieldEquityPartners(BulkIssueConsentRun run,
                                                              List<BulkIssueConsentsTask> tasks) {

    var tasksByOrganisationUnitId = tasks.stream()
        .flatMap(task -> consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task.getConsent())
            .stream()
            .map(ConsentFieldEquityPartner::getOrganisationUnitId)
            .distinct()
            .map(organisationUnitId -> Pair.of(organisationUnitId, task))
        )
        .collect(Collectors.groupingBy(
            Map.Entry::getKey,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
        ));

    tasksByOrganisationUnitId.forEach((organisationUnitId, bulkTasks) -> {
      var successfulApplications = getSuccessfulApplications(tasks);
      var formattedSuccessfulApplications = formatStringList(successfulApplications);

      var organisationUnitWithGroupsJson = organisationUnitService
          .getOrganisationUnitWithGroupsById(organisationUnitId, ORGANISATION_LOOKUP_PURPOSE);

      var emailMergedTemplate = emailService
            .getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_FIELD_EQUITY_PARTNER)
            .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, organisationUnitWithGroupsJson.name())
          .withMailMergeField(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued")
          .withMailMergeField(WORK_AREA_URL_MERGE_FIELD_NAME,
              absoluteUrlService.getAbsoluteUrl(
                  ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))))
          .withMailMergeField(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
              formattedSuccessfulApplications.isEmpty()
                  ? ""
                  : String.format(SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT, formattedSuccessfulApplications))
          .merge();

      consentEmailService.sendConsentIssuedEmailToFieldEquityPartner(organisationUnitWithGroupsJson, run, emailMergedTemplate);
    });
  }

  List<String> getSuccessfulApplications(Collection<BulkIssueConsentsTask> tasks) {
    return tasks.stream()
        .filter(task -> task.getFinishedAt() != null && task.getErrorDetails() == null)
        .map(BulkIssueConsentsTask::getApplicationVersion)
        .sorted(Comparator.comparing(applicationVersion -> applicationVersion.getApplication().getApplicationNo()))
        .map(applicationService::getApplicationReference)
        .toList();
  }

  List<String> getFailedApplications(Collection<BulkIssueConsentsTask> tasks) {
    return tasks.stream()
        .filter(task -> task.getFinishedAt() != null && task.getErrorDetails() != null)
        .map(BulkIssueConsentsTask::getApplicationVersion)
        .sorted(Comparator.comparing(applicationVersion -> applicationVersion.getApplication().getApplicationNo()))
        .map(applicationService::getApplicationReference)
        .toList();
  }

  String formatStringList(List<String> list) {
    if (list.isEmpty()) {
      return "";
    }
    if (list.size() == 1) {
      return "* %s".formatted(list.getFirst());
    }
    return "* %s".formatted(String.join(System.lineSeparator() + "* ", list.subList(0, list.size())));
  }
}

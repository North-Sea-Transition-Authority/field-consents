package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationTechnicalReviews.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewQueryService.APPLICATION_CONSULTATIONS_QUERY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewQueryService.APPLICATION_TECHNICAL_REVIEWS_QUERY;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class WorkAreaFilterService {

  public static final String FIELD_LOOKUP_PURPOSE = "Lookup field for the work-area";

  private final AssetService assetService;
  private final TeamService teamService;
  private final ApplicationDataFilterService applicationDataFilterService;

  WorkAreaFilterService(
      AssetService assetService,
      TeamService teamService,
      ApplicationDataFilterService applicationDataFilterService
  ) {
    this.assetService = assetService;
    this.teamService = teamService;
    this.applicationDataFilterService = applicationDataFilterService;
  }

  List<Condition> getConditions(WorkAreaFilter filter, ServiceUserDetail user, WorkAreaTab workAreaTab) {
    var conditions = new ArrayList<Condition>();

    conditions.add(getApplicationStatusCondition(user));
    conditions.addAll(applicationDataFilterService.getConditions(filter));
    conditions.addAll(getFieldAssetConditions(filter));
    conditions.addAll(getRegulatorConditions(filter));

    if (Objects.isNull(workAreaTab)) {
      return conditions;
    }

    conditions.add(getWorkAreaTabCondition(workAreaTab, user));

    return conditions;
  }

  private Condition getWorkAreaTabCondition(WorkAreaTab workAreaTab, ServiceUserDetail user) {
    return switch (workAreaTab) {
      case MY_APPLICATIONS ->
          APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue())
          .and(APPLICATION_VERSIONS.CURRENT_CASE_OWNER.eq(RegulatorTeamRole.CASE_OFFICER.name()));
      case MY_TECHNICAL_REVIEWS ->
          APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID)
              .eq(user.wuaId().intValue());
      case ALL_TECHNICAL_REVIEWS ->
          APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID).isNotNull();
      case ALL_APPLICATIONS -> DSL.trueCondition();
      case UNASSIGNED_APPLICATIONS -> APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull();
      case ALL_CONSULTATIONS ->
          APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID).isNotNull();
      case UNASSIGNED_CONSULTATIONS ->
          APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID).isNull()
              .and(APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.STATUS).eq(ConsultationStatus.OPEN.name()));
      case MY_CONSULTATIONS ->
          APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID).eq(user.wuaId().intValue());
      case MY_CAM_APPLICATIONS ->
          APPLICATION_VERSIONS.CAM_WUA_ID.eq(user.wuaId().intValue())
          .and(APPLICATION_VERSIONS.CURRENT_CASE_OWNER.eq(RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER.name()));
    };
  }

  private List<Condition> getFieldAssetConditions(WorkAreaFilter filter) {
    var conditions = new ArrayList<Condition>();

    Optional.ofNullable(filter.getAssetKey())
        .map(AssetKey::from)
        .flatMap(assetService::findAsset)
        .map(this::getAssetCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filter.getGeographicAreas())
        .map(applicationDataFilterService::getGeographicAreasQueryCondition)
        .ifPresent(conditions::add);

    return conditions;
  }

  private Condition getAssetCondition(AssetJson assetJson) {
    return APPLICATION_ASSETS.ASSET_TYPE.eq(assetJson.getAssetType().name())
        .and(APPLICATION_ASSETS.ASSET_ID.eq(assetJson.getId()));
  }

  public WorkAreaFilter getDefaultFilter(ServiceUserDetail user) {
    var defaultFilter = new WorkAreaFilter();

    if (teamService.isIndustryUser(user)) {
      defaultFilter.setStatuses(
          List.of(
              ApplicationVersionStatus.IN_PROGRESS,
              ApplicationVersionStatus.AWAITING_PAYMENT,
              ApplicationVersionStatus.SUBMITTED
          )
      );
    }

    defaultFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return defaultFilter;
  }

  private Condition getApplicationStatusCondition(ServiceUserDetail user) {
    if (teamService.isRegulatorUser(user)) {
      return applicationDataFilterService.getSubmittedApplicationStatusCondition();
    }

    if (teamService.isConsulteeUser(user)) {
      return applicationDataFilterService.getSubmittedApplicationStatusCondition();
    }

    if (teamService.isIndustryUser(user)) {
      return getIndustryApplicationStatusCondition();
    }

    throw new IllegalStateException(
        "Expected user [%s] to be regulator, consultee or industry but was none of these.".formatted(user.wuaId())
    );
  }

  private Condition getIndustryApplicationStatusCondition() {
    return APPLICATION_VERSIONS.STATUS.in(
        ApplicationVersionStatus.IN_PROGRESS.name(),
        ApplicationVersionStatus.AWAITING_PAYMENT.name(),
        ApplicationVersionStatus.SUBMITTED.name()
    );
  }

  private List<Condition> getRegulatorConditions(WorkAreaFilter filter) {
    var conditions = new ArrayList<Condition>();

    Optional.ofNullable(filter.getCaseOfficerWuaId())
        .map(this::getCaseOfficerAssignedCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filter.getTechnicalReviewerWuaId())
        .map(this::getTechnicalReviewerAssignedCondition)
        .ifPresent(conditions::add);

    if (Boolean.TRUE.equals(filter.getApprovedForIssue())) {
      conditions.add(applicationDataFilterService.getApprovedForIssueCondition());
    }
    return conditions;
  }

  private Condition getCaseOfficerAssignedCondition(Long caseOfficerWuaId) {
    return APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(caseOfficerWuaId.intValue());
  }

  private Condition getTechnicalReviewerAssignedCondition(Long technicalReviewerWuaId) {
    return APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID)
        .eq(technicalReviewerWuaId.intValue());
  }
}

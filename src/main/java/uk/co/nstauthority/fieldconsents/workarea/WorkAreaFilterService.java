package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationTechnicalReviews.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationFieldService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
public class WorkAreaFilterService {

  public static final String FIELD_LOOKUP_PURPOSE = "Lookup field for the work-area";

  private final AssetService assetService;
  private final FieldService fieldService;
  private final ApplicationFieldService applicationFieldService;
  private final TeamService teamService;
  private final ApplicationDataFilterService applicationDataFilterService;

  public WorkAreaFilterService(AssetService assetService,
                               FieldService fieldService,
                               ApplicationFieldService applicationFieldService,
                               TeamService teamService,
                               ApplicationDataFilterService applicationDataFilterService) {
    this.assetService = assetService;
    this.fieldService = fieldService;
    this.applicationFieldService = applicationFieldService;
    this.teamService = teamService;
    this.applicationDataFilterService = applicationDataFilterService;
  }

  List<Condition> getConditions(WorkAreaFilter filter, ServiceUserDetail user, WorkAreaTab workAreaTab) {
    var conditions = new ArrayList<Condition>();

    conditions.add(getApplicationStatusCondition(user));
    conditions.addAll(applicationDataFilterService.getConditions(filter));
    conditions.addAll(getFieldAssetConditions(filter));

    if (Objects.isNull(workAreaTab)) {
      return conditions;
    }

    conditions.add(getWorkAreaTabCondition(workAreaTab, user));

    return conditions;
  }

  private Condition getWorkAreaTabCondition(WorkAreaTab workAreaTab, ServiceUserDetail user) {
    return switch (workAreaTab) {
      case MY_APPLICATIONS -> APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue());
      case MY_TECHNICAL_REVIEWS -> APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.eq(user.wuaId().intValue());
      case ALL_TECHNICAL_REVIEWS -> APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.isNotNull();
      case ALL_APPLICATIONS -> DSL.trueCondition();
      case UNASSIGNED_APPLICATIONS -> APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull();
      case ALL_CONSULTATIONS -> APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull();
      case UNASSIGNED_CONSULTATIONS -> APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID.isNull()
          .and(APPLICATION_CONSULTATIONS.STATUS.eq(ConsultationStatus.OPEN.name()));
      case MY_CONSULTATIONS -> APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID.eq(user.wuaId().intValue());
    };
  }

  private List<Condition> getFieldAssetConditions(WorkAreaFilter filter) {
    var conditions = new ArrayList<Condition>();

    Optional.ofNullable(filter.getAssetKey())
        .map(AssetKey::from)
        .flatMap(assetKey -> assetService.getAsset(assetKey, FIELD_LOOKUP_PURPOSE))
        .map(this::getAssetCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filter.getGeographicAreas())
        .map(this::getGeographicAreasQueryCondition)
        .ifPresent(conditions::add);

    return conditions;
  }

  public Condition getGeographicAreasQueryCondition(List<GeographicArea> geographicAreas) {
    var primaryFieldIdsInGeographicAreas = fieldService
        .findFieldsByIds(applicationFieldService.findDistinctPrimaryFieldIds(), FIELD_LOOKUP_PURPOSE)
        .stream()
        .filter(fieldJson -> geographicAreas.contains(fieldJson.getGeographicArea()))
        .map(FieldJson::getId)
        .toList();

    return APPLICATION_ASSETS.FIELD_ID.in(primaryFieldIdsInGeographicAreas);
  }

  private Condition getAssetCondition(AssetJson assetJson) {
    return switch (assetJson.getAssetType()) {
      case FIELD ->  APPLICATION_ASSETS.FIELD_ID.eq(assetJson.getId());
      case TERMINAL -> APPLICATION_ASSETS.TERMINAL_ID.eq(assetJson.getId());
    };
  }

  public WorkAreaFilter getDefaultFilter(ServiceUserDetail user) {
    var defaultFilter = new WorkAreaFilter();

    if (teamService.isIndustryUser(user)) {
      defaultFilter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    }

    defaultFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return defaultFilter;
  }

  private Condition getApplicationStatusCondition(ServiceUserDetail user) {
    if (teamService.isRegulatorUser(user)) {
      return getSubmittedApplicationStatusCondition();
    }

    if (teamService.isConsulteeUser(user)) {
      return getSubmittedApplicationStatusCondition();
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
        ApplicationVersionStatus.SUBMITTED.name()
    );
  }

  private Condition getSubmittedApplicationStatusCondition() {
    return APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name());
  }
}

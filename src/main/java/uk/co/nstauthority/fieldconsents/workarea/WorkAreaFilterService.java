package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationTechnicalReviews.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.FIELD_LOOKUP_PURPOSE;

import java.util.List;
import java.util.Objects;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationFieldService;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
public class WorkAreaFilterService {

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
    var conditions = applicationDataFilterService.getConditions(filter);

    if (Objects.nonNull(filter.getAssetKey())) {
      var assetJsonOptional = assetService.getAssetFromKey(filter.getAssetKey());
      assetJsonOptional.ifPresent(assetJson -> conditions.add(getAssetCondition(assetJson)));
    }

    if (Objects.nonNull(filter.getGeographicAreas())) {
      conditions.add(
          getGeographicAreasQueryCondition(filter.getGeographicAreas())
      );
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.MY_APPLICATIONS.equals(workAreaTab)) {
      conditions.add(getMyApplicationsCaseOfficerCondition(user));
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.MY_TECHNICAL_REVIEWS.equals(workAreaTab)) {
      conditions.add(getMyTechnicalReviewsCondition(user));
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.ALL_TECHNICAL_REVIEWS.equals(workAreaTab)) {
      conditions.add(getAllTechnicalReviewsCondition());
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.UNASSIGNED_APPLICATIONS.equals(workAreaTab)) {
      conditions.add(getUnassignedApplicationsCaseOfficerCondition());
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.ALL_CONSULTATIONS.equals(workAreaTab)) {
      conditions.add(getAllConsultationsCondition());
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.UNASSIGNED_CONSULTATIONS.equals(workAreaTab)) {
      conditions.add(getUnassignedConsultationsCondition());
    }

    addApplicationStatusCondition(conditions, user);

    return conditions;
  }

  public Condition getGeographicAreasQueryCondition(List<GeographicArea> geographicAreas) {
    var primaryFieldIdsInGeographicAreas = fieldService
        .findFieldsByIds(
            applicationFieldService.findDistinctPrimaryFieldIds(),
            FIELD_LOOKUP_PURPOSE
        )
        .stream()
        .filter(fieldJson -> geographicAreas.contains(fieldJson.getGeographicArea()))
        .map(FieldJson::getId)
        .toList();
    return APPLICATION_ASSETS.FIELD_ID.in(primaryFieldIdsInGeographicAreas);
  }

  private Condition getAssetCondition(AssetJson assetJson) {
    if (AssetType.FIELD.equals(assetJson.getAssetType())) {
      return APPLICATION_ASSETS.FIELD_ID.eq(assetJson.getId());
    } else if (AssetType.TERMINAL.equals(assetJson.getAssetType())) {
      return APPLICATION_ASSETS.TERMINAL_ID.eq(assetJson.getId());
    } else {
      throw new RuntimeException("Not a valid Asset Type: " + assetJson.getAssetType());
    }
  }

  public WorkAreaFilter getDefaultFilter(ServiceUserDetail user) {
    var defaultFilter = new WorkAreaFilter();

    if (teamService.isIndustryUser(user)) {
      defaultFilter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    }

    defaultFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return defaultFilter;
  }

  private Condition getMyApplicationsCaseOfficerCondition(ServiceUserDetail user) {
    return APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue());
  }

  private Condition getMyTechnicalReviewsCondition(ServiceUserDetail user) {
    return APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.eq(user.wuaId().intValue());
  }

  private Condition getAllTechnicalReviewsCondition() {
    return APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.isNotNull();
  }

  private Condition getUnassignedApplicationsCaseOfficerCondition() {
    return APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull();
  }

  private Condition getAllConsultationsCondition() {
    return APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull();
  }

  // TODO FCS-394 this needs changing when allocation is done
  // i.e. check that the consultation has null responder
  private Condition getUnassignedConsultationsCondition() {
    return APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull();
  }

  private void addApplicationStatusCondition(List<Condition> conditions, ServiceUserDetail user) {
    if (teamService.isRegulatorUser(user) || teamService.isConsulteeUser(user)) {
      conditions.add(getSubmittedApplicationStatusCondition());
    } else if (teamService.isIndustryUser(user)) {
      conditions.add(getIndustryApplicationStatusCondition());
    }
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

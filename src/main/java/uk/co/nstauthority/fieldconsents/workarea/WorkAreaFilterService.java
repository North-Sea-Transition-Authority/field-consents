package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormService.FIELD_LOOKUP_PURPOSE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationFieldService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationTerminalService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
public class WorkAreaFilterService {

  private final AssetService assetService;

  private final FieldService fieldService;

  private final ApplicationFieldService applicationFieldService;

  private final ApplicationTerminalService applicationTerminalService;

  private final TeamService teamService;

  public WorkAreaFilterService(AssetService assetService,
                               FieldService fieldService,
                               ApplicationFieldService applicationFieldService,
                               ApplicationTerminalService applicationTerminalService,
                               TeamService teamService) {
    this.assetService = assetService;
    this.fieldService = fieldService;
    this.applicationFieldService = applicationFieldService;
    this.applicationTerminalService = applicationTerminalService;
    this.teamService = teamService;
  }

  ArrayList<Condition> getConditions(WorkAreaFilter filter, ServiceUserDetail user, WorkAreaTab workAreaTab)  {
    var conditions = new ArrayList<Condition>();

    if (Objects.nonNull(filter.getStatuses())) {
      conditions.add(getStatusQueryCondition(filter.getStatuses()));
    }

    if (Objects.nonNull(filter.getReferenceNumber())) {
      conditions.add(getReferenceNumberQueryCondition(filter.getReferenceNumber()));
    }

    if (Objects.nonNull(filter.getApplicationTypes())) {
      conditions.add(getApplicationTypesQueryCondition(filter.getApplicationTypes()));
    }

    if (Objects.nonNull(filter.getDurationTypes())) {
      conditions.add(getDurationTypesQueryCondition(filter.getDurationTypes()));
    }

    if (Objects.nonNull(filter.getOperatorId())) {
      conditions.add(getOperatorCondition(filter.getOperatorId()));
    }

    if (Objects.nonNull(filter.getAssetKey())) {
      var assetJsonOptional = assetService.getAssetFromKey(filter.getAssetKey());
      assetJsonOptional.ifPresent(assetJson -> conditions.add(getAssetCondition(assetJson)));
    }

    if (Objects.nonNull(filter.getGeographicAreas())) {
      conditions.add(getGeographicAreasQueryCondition(filter.getGeographicAreas()));
    }

    if (Objects.nonNull(filter.getAssetTypesWithShore())) {
      conditions.add(getAssetTypesQueryCondition(filter.getAssetTypesWithShore()));
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.MY_APPLICATIONS.equals(workAreaTab)) {
      conditions.add(getMyApplicationsCaseOfficerCondition(user));
    }

    if (Objects.nonNull(workAreaTab) && WorkAreaTab.UNASSIGNED_APPLICATIONS.equals(workAreaTab)) {
      conditions.add(getUnassignedApplicationsCaseOfficerCondition());
    }

    if (teamService.isRegulatorUser(user)) {
      conditions.add(getRegulatorApplicationStatusCondition());
    }

    if (teamService.isIndustryUser(user)) {
      conditions.add(getIndustryApplicationStatusCondition());
    }
    return conditions;
  }

  private Condition getReferenceNumberQueryCondition(String referenceNumber) {
    return APPLICATIONS.APPLICATION_NO.cast(String.class).eq(referenceNumber);
  }

  private Condition getStatusQueryCondition(List<ApplicationVersionStatus> statuses) {
    var statusStrings = statuses
        .stream()
        .map(ApplicationVersionStatus::getEnumName)
        .toList();
    return APPLICATION_VERSIONS.STATUS.in(statusStrings);
  }

  private Condition getApplicationTypesQueryCondition(List<ApplicationType> applicationTypes) {
    var applicationTypeStrings = applicationTypes
        .stream()
        .map(ApplicationType::getEnumName)
        .toList();
    return APPLICATIONS.TYPE.in(applicationTypeStrings);
  }

  private Condition getDurationTypesQueryCondition(List<ConsentLengthType> durationTypes) {
    var consentLengthStrings = durationTypes
        .stream()
        .map(ConsentLengthType::getEnumName)
        .toList();
    return CONSENT_LENGTHS.CONSENT_LENGTH.in(consentLengthStrings);
  }

  private Condition getGeographicAreasQueryCondition(List<GeographicArea> geographicAreas) {
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

  private Condition getAssetTypesQueryCondition(List<AssetTypeWithShore> assetTypes) {
    if (containsTerminalOnly(assetTypes)) {

      return APPLICATION_ASSETS.TERMINAL_ID.in(applicationTerminalService.findDistinctPrimaryTerminalIds());

    } else if (containsFieldsOnly(assetTypes)) {

      return APPLICATION_ASSETS.FIELD_ID.in(getPrimaryFieldIdsOfShoreType(assetTypes));
    } else {

      return APPLICATION_ASSETS.FIELD_ID.in(getPrimaryFieldIdsOfShoreType(assetTypes))
          .or(APPLICATION_ASSETS.TERMINAL_ID.in(applicationTerminalService.findDistinctPrimaryTerminalIds()));
    }
  }

  private static boolean containsFieldsOnly(List<AssetTypeWithShore> assetTypeWithShores) {
    var terminalOnly = assetTypeWithShores
        .stream()
        .filter(AssetTypeWithShore::isTerminal)
        .toList();

    return terminalOnly.isEmpty();
  }

  private static boolean containsTerminalOnly(List<AssetTypeWithShore> assetTypeWithShores) {
    var fieldsOnly = assetTypeWithShores
        .stream()
        .filter(AssetTypeWithShore::isField)
        .toList();

    return fieldsOnly.isEmpty();
  }

  private List<Integer> getPrimaryFieldIdsOfShoreType(List<AssetTypeWithShore> assetTypes) {
    return fieldService
        .findFieldsByIds(
            applicationFieldService.findDistinctPrimaryFieldIds(),
            FIELD_LOOKUP_PURPOSE
        )
        .stream()
        .filter(fieldJson -> assetTypes.stream()
            .map(AssetTypeWithShore::getShore)
            .toList()
            .contains(fieldJson.getShore()))
        .map(FieldJson::getId)
        .toList();
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

  private Condition getOperatorCondition(Integer operatorId) {
    return APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(operatorId);
  }

  public WorkAreaFilter getDefaultFilter(ServiceUserDetail user) {
    var defaultFilter = new WorkAreaFilter();

    if (!teamService.isRegulatorUser(user)) {
      defaultFilter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    }

    defaultFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return defaultFilter;
  }

  private Condition getMyApplicationsCaseOfficerCondition(ServiceUserDetail user) {
    return APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue());
  }

  private Condition getUnassignedApplicationsCaseOfficerCondition() {
    return APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull();
  }

  private Condition getIndustryApplicationStatusCondition() {
    return APPLICATION_VERSIONS.STATUS.notEqual(ApplicationVersionStatus.COMPLETED.name());
  }

  private Condition getRegulatorApplicationStatusCondition() {
    return APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name());
  }
}

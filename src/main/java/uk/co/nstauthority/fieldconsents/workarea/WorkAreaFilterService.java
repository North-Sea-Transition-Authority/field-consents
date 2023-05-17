package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@Service
public class WorkAreaFilterService {

  private final AssetService assetService;

  public WorkAreaFilterService(AssetService assetService) {
    this.assetService = assetService;
  }

  ArrayList<Condition> getConditions(WorkAreaFilter filter)  {
    var conditions = new ArrayList<Condition>();

    if (Objects.nonNull(filter.getStatuses())) {
      conditions.add(getStatusQueryCondition(filter.getStatuses()));
    }

    // TODO: uncomment this on FCS-326: Add work-area filter for application reference and sea location
    //    if (StringUtils.isNotBlank(filter.getReference())) {
    //      conditions.add(getReferenceQueryCondition(filter.getReference()));
    //    }

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

    return conditions;
  }

  private Condition getStatusQueryCondition(List<ApplicationVersionStatus> statuses) {
    var statusStrings = statuses
        .stream()
        .map(ApplicationVersionStatus::getEnumName)
        .toList();
    return APPLICATION_VERSIONS.STATUS.in(statusStrings);
  }

  // TODO: Implement this FCS-326: Add work-area filter for application reference and sea location
  //    private Condition getReferenceQueryCondition(String reference) {
  //      return null;
  //    }

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
}

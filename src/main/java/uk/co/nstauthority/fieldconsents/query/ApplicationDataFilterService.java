package uk.co.nstauthority.fieldconsents.query;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;

@Service
public class ApplicationDataFilterService {

  public static final String FIELD_LOOKUP_PURPOSE = "Lookup field for application data";

  private final DSLContext context;
  private final ApplicationAssetService applicationAssetService;

  public ApplicationDataFilterService(DSLContext context,
                                      ApplicationAssetService applicationAssetService) {
    this.context = context;
    this.applicationAssetService = applicationAssetService;
  }

  Condition getApplicationNumberQueryCondition(Integer applicationNumber) {
    return APPLICATIONS.APPLICATION_NO.eq(applicationNumber);
  }

  Condition getStatusQueryCondition(List<ApplicationVersionStatus> statuses) {
    var statusStrings = statuses
        .stream()
        .map(ApplicationVersionStatus::getEnumName)
        .toList();
    return APPLICATION_VERSIONS.STATUS.in(statusStrings);
  }

  Condition getApplicationTypesQueryCondition(List<ApplicationType> applicationTypes) {
    var applicationTypeStrings = applicationTypes
        .stream()
        .map(ApplicationType::getEnumName)
        .toList();
    return APPLICATIONS.TYPE.in(applicationTypeStrings);
  }

  Condition getDurationTypesQueryCondition(List<ConsentLengthType> durationTypes) {
    var consentLengthStrings = durationTypes
        .stream()
        .map(ConsentLengthType::getEnumName)
        .toList();
    return CONSENT_LENGTHS.CONSENT_LENGTH.in(consentLengthStrings);
  }

  Condition getOperatorCondition(Integer operatorId) {
    return APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(operatorId);
  }

  Condition getAssetTypesQueryCondition(List<AssetTypeWithShore> assetTypes) {
    var conditions = new ArrayList<Condition>();

    if (containsTerminal(assetTypes)) {
      conditions.add(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.TERMINAL.name()));
    }

    if (containsFields(assetTypes)) {
      var primaryAndSecondaryFieldIds = applicationAssetService
          .getPrimaryAndSecondaryFieldJsonsOfShoreType(assetTypes, FIELD_LOOKUP_PURPOSE)
          .stream()
          .map(FieldJson::getId)
          .toList();

      conditions.add(
          exists(context.select(APPLICATION_ASSETS.ASSET_ID)
              .from(APPLICATION_ASSETS)
              .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                  .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                  .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                  .and(APPLICATION_ASSETS.ASSET_ID.in(primaryAndSecondaryFieldIds))))
      );
    }

    if (conditions.size() == 1) {
      return conditions.get(0);
    }

    return conditions.get(0).or(conditions.get(1));
  }

  private Condition getSubmittedYearQueryCondition(Integer submittedYear) {
    return year(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME).eq(submittedYear);
  }

  private static boolean containsFields(List<AssetTypeWithShore> assetTypeWithShores) {
    return assetTypeWithShores
        .stream()
        .anyMatch(AssetTypeWithShore::isField);
  }

  private static boolean containsTerminal(List<AssetTypeWithShore> assetTypeWithShores) {
    return assetTypeWithShores
        .stream()
        .anyMatch(AssetTypeWithShore::isTerminal);
  }

  public List<Condition> getConditions(ApplicationDataFilterForm dataFilterForm) {
    var conditions = new ArrayList<Condition>();

    Optional.ofNullable(dataFilterForm.getStatuses())
        .map(this::getStatusQueryCondition)
        .ifPresent(conditions::add);

    var referenceNumber = dataFilterForm.getReferenceNumber();
    if (Objects.nonNull(referenceNumber)) {
      if (isNumeric(referenceNumber)) {
        conditions.add(this.getApplicationNumberQueryCondition(Integer.parseInt(referenceNumber)));
      } else {
        conditions.add(DSL.falseCondition());
      }
    }

    Optional.ofNullable(dataFilterForm.getApplicationTypes())
        .map(this::getApplicationTypesQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(dataFilterForm.getDurationTypes())
        .map(this::getDurationTypesQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(dataFilterForm.getOperatorId())
        .map(this::getOperatorCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(dataFilterForm.getAssetTypesWithShore())
      .map(this::getAssetTypesQueryCondition)
          .ifPresent(conditions::add);

    var submittedYear = dataFilterForm.getSubmittedYear();
    if (Objects.nonNull(submittedYear)) {
      if (isNumeric(submittedYear)) {
        conditions.add(this.getSubmittedYearQueryCondition(Integer.parseInt(submittedYear)));
      } else {
        conditions.add(falseCondition());
      }
    }

    return conditions;
  }
}

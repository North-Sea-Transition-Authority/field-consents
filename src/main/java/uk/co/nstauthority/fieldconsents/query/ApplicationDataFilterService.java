package uk.co.nstauthority.fieldconsents.query;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static uk.co.nstauthority.fieldconsents.assets.AssetType.FIELD;
import static uk.co.nstauthority.fieldconsents.assets.AssetType.TERMINAL;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsentIssuingApprovals.APPLICATION_CONSENT_ISSUING_APPROVALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationFlags.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationFieldService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;

@Service
public class ApplicationDataFilterService {

  public static final String UNASSIGNED = "unassigned";
  public static final String FIELD_LOOKUP_PURPOSE = "Lookup field for application data";
  public static final String TERMINAL_LOOKUP_PURPOSE = "Lookup terminal for application data";

  private final DSLContext context;
  private final ApplicationAssetService applicationAssetService;
  private final FieldService fieldService;
  private final TerminalService terminalService;
  private final ApplicationFieldService applicationFieldService;

  ApplicationDataFilterService(
      DSLContext context,
      ApplicationAssetService applicationAssetService,
      FieldService fieldService,
      TerminalService terminalService,
      ApplicationFieldService applicationFieldService
  ) {
    this.context = context;
    this.applicationAssetService = applicationAssetService;
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationFieldService = applicationFieldService;
  }

  Condition getApplicationNumberQueryCondition(Integer applicationNumber) {
    return APPLICATIONS.APPLICATION_NO.eq(applicationNumber);
  }

  public Condition getSubmittedApplicationStatusCondition() {
    return APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name());
  }

  Condition getStatusQueryCondition(Collection<ApplicationVersionStatus> statuses) {
    var statusStrings = statuses
        .stream()
        .map(ApplicationVersionStatus::getEnumName)
        .toList();
    return APPLICATION_VERSIONS.STATUS.in(statusStrings);
  }

  Condition getApplicationTypesQueryCondition(Collection<ApplicationType> applicationTypes) {
    var applicationTypeStrings = applicationTypes
        .stream()
        .map(ApplicationType::getEnumName)
        .toList();
    return APPLICATIONS.TYPE.in(applicationTypeStrings);
  }

  Condition getDurationTypesQueryCondition(Collection<ConsentLengthType> durationTypes) {
    var consentLengthStrings = durationTypes
        .stream()
        .map(ConsentLengthType::getEnumName)
        .toList();
    return CONSENT_LENGTHS.CONSENT_LENGTH.in(consentLengthStrings);
  }

  public Condition getOperatorCondition(Integer operatorId) {
    return APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(operatorId);
  }

  public Condition getGeographicAreasQueryCondition(Collection<GeographicArea> geographicAreas) {
    var primaryFieldIdsInGeographicAreas = fieldService
        .findFieldsByIds(applicationFieldService.findDistinctPrimaryFieldIds(), FIELD_LOOKUP_PURPOSE)
        .stream()
        .filter(fieldJson -> geographicAreas.contains(fieldJson.getGeographicArea()))
        .map(FieldJson::getId)
        .toList();

    return APPLICATION_ASSETS.ASSET_TYPE.eq(FIELD.name())
        .and(APPLICATION_ASSETS.ASSET_ID.in(primaryFieldIdsInGeographicAreas));
  }

  public Condition getFieldCondition(AssetKey assetKey) {
    throwIfAssetKeyInvalidType(assetKey, FIELD);

    var fieldJson = fieldService.getField(assetKey.assetId(), FIELD_LOOKUP_PURPOSE);

    return exists(context.select(APPLICATION_ASSETS.ASSET_ID)
        .from(APPLICATION_ASSETS)
        .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
            .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
            .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
            .and(APPLICATION_ASSETS.ASSET_ID.eq(fieldJson.getId()))));
  }

  public Condition getTerminalCondition(AssetKey assetKey) {
    throwIfAssetKeyInvalidType(assetKey, TERMINAL);

    var terminalJson = terminalService.getTerminal(assetKey.assetId(), TERMINAL_LOOKUP_PURPOSE);

    return exists(context.select(APPLICATION_ASSETS.ASSET_ID)
        .from(APPLICATION_ASSETS)
        .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
            .and(APPLICATION_ASSETS.ASSET_ROLE.eq(AssetRole.PRIMARY.name()))
            .and(APPLICATION_ASSETS.ASSET_TYPE.eq(TERMINAL.name()))
            .and(APPLICATION_ASSETS.ASSET_ID.eq(terminalJson.getId()))));
  }

  private void throwIfAssetKeyInvalidType(AssetKey assetKey, AssetType expectedType) {
    if (assetKey.assetType() == expectedType) {
      return;
    }

    throw new IllegalArgumentException("Expected AssetKey.assetType to be [%s] but was [%s]".formatted(
        expectedType,
        assetKey.assetType()
    ));
  }

  public Condition getAceStatusCondition(Collection<AceFlagStatus> aceFlagStatuses) {
    var aceFlagIsAceApplicationValues = aceFlagStatuses.stream().map(AceFlagStatus::isAceApplication).toList();

    return exists(context.select(APPLICATION_FLAGS.FLAG_VALUE)
        .from(APPLICATION_FLAGS)
        .where(APPLICATION_FLAGS.FLAG_TYPE.eq(ApplicationFlagType.IS_ACE_APPLICATION.name())
            .and(APPLICATION_FLAGS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID))
            .and(APPLICATION_FLAGS.FLAG_VALUE.in(aceFlagIsAceApplicationValues)))
    );
  }

  public Condition getApprovedForIssueCondition() {
    return APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull();
  }

  public Condition getAssetTypesQueryCondition(Collection<AssetTypeWithShore> assetTypes) {
    var conditions = new ArrayList<Condition>();

    if (containsAssetOfType(assetTypes, TERMINAL)) {
      conditions.add(APPLICATION_ASSETS.ASSET_TYPE.eq(TERMINAL.name()));
    }

    if (containsAssetOfType(assetTypes, AssetType.FIELD)) {
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
      return conditions.getFirst();
    }

    return conditions.get(0).or(conditions.get(1));
  }

  private Condition getSubmittedYearQueryCondition(Integer submittedYear) {
    return year(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME).eq(submittedYear);
  }

  private static boolean containsAssetOfType(Collection<AssetTypeWithShore> assetTypeWithShores, AssetType assetType) {
    return assetTypeWithShores.stream().map(AssetTypeWithShore::getAssetType).anyMatch(assetType::equals);
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

    var licenceReference = dataFilterForm.getLicenceReference();
    if (Objects.nonNull(licenceReference)) {
      List<Integer> fieldIdsWithMatchingLicence = getFieldIdsWithMatchingLicence(licenceReference);

      if (!fieldIdsWithMatchingLicence.isEmpty()) {
        conditions.add(getLicenceReferenceQueryCondition(fieldIdsWithMatchingLicence));
      } else {
        conditions.add(falseCondition());
      }
    }

    return conditions;
  }

  private Condition getLicenceReferenceQueryCondition(List<Integer> fieldIdsWithMatchingLicence) {
    return
        exists(context.select(APPLICATION_ASSETS.ASSET_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                .and(APPLICATION_ASSETS.ASSET_ID.in(fieldIdsWithMatchingLicence))));
  }

  private List<Integer> getFieldIdsWithMatchingLicence(String licenceReference) {
    var primaryAndSecondaryFieldIds = applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()
        .stream()
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    if (primaryAndSecondaryFieldIds.isEmpty()) {
      return Collections.emptyList();
    }

    return fieldService
        .findFieldsWithOperatorAndLicences(primaryAndSecondaryFieldIds, FIELD_LOOKUP_PURPOSE)
        .stream()
        .filter(fieldJson -> fieldJson.getLicenceReferences().contains(licenceReference))
        .map(FieldJson::getId)
        .toList();
  }
}

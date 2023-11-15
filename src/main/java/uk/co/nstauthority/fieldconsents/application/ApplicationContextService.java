package uk.co.nstauthority.fieldconsents.application;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetWithLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.licences.LicenceJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@Service
public class ApplicationContextService {

  private final ConsentLengthService consentLengthService;
  private final ApplicationAssetService applicationAssetService;
  private final OrganisationUnitService organisationUnitService;
  private final FieldService fieldService;
  private final TerminalService terminalService;

  ApplicationContextService(
      ConsentLengthService consentLengthService,
      ApplicationAssetService applicationAssetService,
      OrganisationUnitService organisationUnitService,
      FieldService fieldService,
      TerminalService terminalService
  ) {
    this.consentLengthService = consentLengthService;
    this.applicationAssetService = applicationAssetService;
    this.organisationUnitService = organisationUnitService;
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  public SummaryCard getApplicationContextSummaryCard(ApplicationVersion applicationVersion) {
    var applicationContext = getApplicationContext(applicationVersion);

    var summaryData = SummaryDataView
        .newWithKeyValue("Application type", applicationVersion.getApplication().getType().getDisplayName())
        .addKeyValue(applicationContext.getPrimaryAssetPrompt(), applicationContext.primaryAsset().getName())
        .addKeyValue(applicationContext.getPrimaryOperatorPrompt(), applicationContext.primaryOperator());

    return SummaryCard.simpleSummaryCard(summaryData);
  }

  public ApplicationContext getApplicationContext(ApplicationVersion applicationVersion) {
    var applicationContextBuilder = ApplicationContext.newBuilder();

    addPrimaryOperator(applicationVersion, applicationContextBuilder);
    addApplicationVersionStatus(applicationVersion, applicationContextBuilder);

    var consentLengthDetails = consentLengthService.findConsentLengthDetails(applicationVersion).orElse(null);
    addConsentLength(consentLengthDetails, applicationContextBuilder);
    addConsentStartYear(consentLengthDetails, applicationContextBuilder);

    addAssetDetailsToContext(applicationVersion, applicationContextBuilder);

    return applicationContextBuilder.build();
  }

  void addPrimaryOperator(ApplicationVersion applicationVersion, ApplicationContext.Builder builder) {
    var primaryOperator = organisationUnitService.getOrganisationUnitByIdOrFallback(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation lookup for application context information",
        applicationVersion.getCachedPrimaryOperatorName()
    );

    builder.withPrimaryOperator(primaryOperator.name());
  }

  void addApplicationVersionStatus(ApplicationVersion applicationVersion, ApplicationContext.Builder builder) {
    builder.withApplicationVersionStatus(applicationVersion.getStatus());
  }

  void addConsentLength(@Nullable ConsentLengthDetails consentLengthDetails, ApplicationContext.Builder builder) {
    Optional.ofNullable(consentLengthDetails)
        .map(ConsentLengthDetails::getConsentLength)
        .ifPresent(builder::withConsentDuration);
  }

  void addConsentStartYear(@Nullable ConsentLengthDetails consentLengthDetails, ApplicationContext.Builder builder) {
    Optional.ofNullable(consentLengthDetails)
        .map(cld -> switch (cld.getConsentLength()) {
          case ANNUAL -> cld.getAnnualConsentYear();
          case SHORT_TERM -> cld.getShortTermStartDate().getYear();
          case LONG_TERM -> cld.getLongTermStartYear();
        })
        .map(String::valueOf)
        .ifPresent(builder::withConsentStartYear);
  }

  void addAssetDetailsToContext(ApplicationVersion applicationVersion, ApplicationContext.Builder builder) {
    var applicationAssets = applicationAssetService
        .findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY));

    var requestPurpose = "Asset lookup for application context";

    var fieldIds = applicationAssets.stream()
        .filter(ApplicationAsset::isField)
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    var fieldsById = fieldService
        .findFieldsWithOperatorAndLicences(fieldIds, requestPurpose)
        .stream()
        .collect(Collectors.toMap(FieldWithOperatorAndLicencesJson::getId, Function.identity()));

    var terminalIds = applicationAssets.stream()
        .filter(ApplicationAsset::isTerminal)
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    var terminalsById = terminalService.findTerminalsWithOperator(terminalIds, requestPurpose)
        .stream()
        .collect(Collectors.toMap(TerminalWithOperatorJson::getId, Function.identity()));

    var secondaryFields = applicationAssets
        .stream()
        .filter(ApplicationAsset::isField)
        .filter(applicationAsset -> applicationAsset.getAssetRole() == AssetRole.SECONDARY)
        .map(ApplicationAsset::getAssetId)
        .map(fieldsById::get)
        .toList();

    var allAssets = Stream.concat(fieldsById.values().stream(), terminalsById.values().stream()).toList();
    addPrimaryAsset(applicationAssets, allAssets, builder);
    addAssetOperators(allAssets, builder);

    addAdditionalFields(secondaryFields, builder);
    addLicences(fieldsById.values(), builder);
  }

  void addPrimaryAsset(
      Collection<ApplicationAsset> applicationAssets,
      Collection<? extends AssetJson> assetJsons,
      ApplicationContext.Builder builder
  ) {
    var primaryApplicationAsset = applicationAssets.stream()
        .filter(applicationAsset -> applicationAsset.getAssetRole() == AssetRole.PRIMARY)
        .findFirst()
        .orElseThrow();

    var primaryAsset = assetJsons.stream()
        .filter(assetJson -> assetJson.getAssetType().equals(primaryApplicationAsset.getAssetType()))
        .filter(assetJson -> assetJson.getId().equals(primaryApplicationAsset.getAssetId()))
        .findFirst()
        .orElseThrow();

    builder.withPrimaryAsset(primaryAsset);
  }

  void addAssetOperators(Collection<AssetWithOperatorJson> assetWithOperatorJsons, ApplicationContext.Builder builder) {
    var operators = assetWithOperatorJsons
        .stream()
        .map(AssetWithOperatorJson::getOperatorJson)
        .map(OrganisationUnitJson::name)
        .collect(Collectors.toSet());

    builder.withAssetOperators(operators);
  }

  void addAdditionalFields(Collection<? extends AssetJson> assetJsons, ApplicationContext.Builder builder) {
    var additionalAssets = assetJsons.stream().map(AssetJson::getName).collect(Collectors.toSet());
    builder.withAdditionalFields(additionalAssets);
  }

  void addLicences(Collection<? extends AssetWithLicencesJson> assetWithLicencesJsons, ApplicationContext.Builder builder) {
    var licences = assetWithLicencesJsons
        .stream()
        .map(AssetWithLicencesJson::getLicences)
        .flatMap(Collection::stream)
        .map(LicenceJson::licenceRef)
        .collect(Collectors.toSet());

    builder.withLicences(licences);
  }

}

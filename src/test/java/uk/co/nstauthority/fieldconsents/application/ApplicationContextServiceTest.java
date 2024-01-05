package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class ApplicationContextServiceTest {

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Spy
  @InjectMocks
  private ApplicationContextService applicationContextService;

  private ApplicationVersion applicationVersion;

  private OrganisationUnitJson primaryOperator;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    primaryOperator = new OrganisationUnitJson(applicationVersion.getPrimaryOperatorOuId(), applicationVersion.getCachedPrimaryOperatorName());
  }

  @Test
  void getApplicationContextSummaryCard_terminal() {
    var applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(terminal1Json)
        .withPrimaryOperator("Smooth operator")
        .build();

    doReturn(applicationContext)
        .when(applicationContextService)
        .getApplicationContext(applicationVersion);

    assertThat(applicationContextService.getApplicationContextSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue("Application type", applicationVersion.getApplication().getType().getDisplayName()),
                new SummaryKeyValue("Primary facility", applicationContext.primaryAsset().getName()),
                new SummaryKeyValue("Primary operator", applicationContext.primaryOperator())
            ))));
  }

  @Test
  void getApplicationContextSummaryCard_field() {
    var applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(field1Json)
        .withPrimaryOperator("Smooth operator")
        .build();

    doReturn(applicationContext)
        .when(applicationContextService)
        .getApplicationContext(applicationVersion);

    var summaryCard = applicationContextService.getApplicationContextSummaryCard(applicationVersion);

    assertThat(summaryCard).usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue("Application type", applicationVersion.getApplication().getType().getDisplayName()),
                new SummaryKeyValue("Primary field", applicationContext.primaryAsset().getName()),
                new SummaryKeyValue("Primary operator", applicationContext.primaryOperator())
            ))));
  }

  @Test
  void getApplicationContext() {
    var builderCaptor = ArgumentCaptor.forClass(ApplicationContext.Builder.class);

    doNothing().when(applicationContextService).addPrimaryOperator(eq(applicationVersion), builderCaptor.capture());
    doNothing().when(applicationContextService).addApplicationVersionStatus(eq(applicationVersion), builderCaptor.capture());

    var consentLengthDetails = mock(ConsentLengthDetails.class);
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));

    doNothing().when(applicationContextService).addConsentLength(eq(consentLengthDetails), builderCaptor.capture());
    doNothing().when(applicationContextService).addConsentStartYear(eq(consentLengthDetails), builderCaptor.capture());

    doNothing().when(applicationContextService).addAssetDetailsToContext(eq(applicationVersion), builderCaptor.capture());

    var emptyBuilder = ApplicationContext.newBuilder();

    assertThat(applicationContextService.getApplicationContext(applicationVersion)).isEqualTo(emptyBuilder.build());
    assertThat(builderCaptor.getAllValues())
        .map(ApplicationContext.Builder::build)
        .allMatch(emptyBuilder.build()::equals);
  }

  @Test
  void addPrimaryOperator() {
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addPrimaryOperator(applicationVersion, builder);
    assertThat(builder.build()).extracting(ApplicationContext::primaryOperator).isEqualTo(primaryOperator.name());
  }

  @ParameterizedTest
  @EnumSource(ApplicationVersionStatus.class)
  void addApplicationVersionStatus(ApplicationVersionStatus status) {
    applicationVersion.setStatus(status);

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addApplicationVersionStatus(applicationVersion, builder);
    assertThat(builder.build()).extracting(ApplicationContext::applicationVersionStatus).isEqualTo(applicationVersion.getStatus().getDisplayName());
  }

  @ParameterizedTest
  @EnumSource(ConsentLengthType.class)
  void addConsentLength(ConsentLengthType consentLengthType) {
    var consentLengthDetails = mock(ConsentLengthDetails.class);
    when(consentLengthDetails.getConsentLength()).thenReturn(consentLengthType);

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addConsentLength(consentLengthDetails, builder);
    assertThat(builder.build()).extracting(ApplicationContext::consentDuration).isEqualTo(consentLengthType.getDisplayName());
  }

  @Test
  void addConsentLength_null() {
    var builder = ApplicationContext.newBuilder();
    applicationContextService.addConsentLength(null, builder);
    assertThat(builder.build()).extracting(ApplicationContext::consentDuration).isNull();
  }

  @Test
  void addConsentStartYear_annual() {
    var consentLengthDetails = mock(ConsentLengthDetails.class);
    when(consentLengthDetails.getConsentLength()).thenReturn(ConsentLengthType.ANNUAL);
    when(consentLengthDetails.getAnnualConsentYear()).thenReturn(2023);

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addConsentStartYear(consentLengthDetails, builder);
    assertThat(builder.build()).extracting(ApplicationContext::consentStartYear).isEqualTo("2023");
  }

  @Test
  void addConsentStartYear_shortTerm() {
    var consentLengthDetails = mock(ConsentLengthDetails.class);
    when(consentLengthDetails.getConsentLength()).thenReturn(ConsentLengthType.SHORT_TERM);
    when(consentLengthDetails.getShortTermStartDate()).thenReturn(LocalDate.of(2023, 1, 1));

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addConsentStartYear(consentLengthDetails, builder);
    assertThat(builder.build()).extracting(ApplicationContext::consentStartYear).isEqualTo("2023");
  }

  @Test
  void addConsentStartYear_longTerm() {
    var consentLengthDetails = mock(ConsentLengthDetails.class);
    when(consentLengthDetails.getConsentLength()).thenReturn(ConsentLengthType.LONG_TERM);
    when(consentLengthDetails.getLongTermStartYear()).thenReturn(2023);

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addConsentStartYear(consentLengthDetails, builder);
    assertThat(builder.build()).extracting(ApplicationContext::consentStartYear).isEqualTo("2023");
  }

  @Test
  void addConsentStartYear_null() {
    var builder = ApplicationContext.newBuilder();
    applicationContextService.addConsentStartYear(null, builder);
    assertThat(builder.build()).extracting(ApplicationContext::consentStartYear).isNull();
  }

  @Test
  void addAssetDetailsToContext() {
    var fields = List.of(field1Json, field2Json);
    var fieldIds = fields.stream().map(AssetJson::getId).toList();
    var terminals = List.of(terminal1Json, terminal2Json);
    var terminalIds = terminals.stream().map(AssetJson::getId).toList();

    var field1ApplicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetRole(AssetRole.PRIMARY)
        .withAssetJson(fields.get(0))
        .build();

    var field2ApplicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetRole(AssetRole.SECONDARY)
        .withAssetJson(fields.get(1))
        .build();

    var terminal1ApplicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetRole(AssetRole.SECONDARY)
        .withAssetJson(terminals.get(0))
        .build();

    var terminal2ApplicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetRole(AssetRole.SECONDARY)
        .withAssetJson(terminals.get(1))
        .build();

    var applicationAssets = List.of(field1ApplicationAsset, field2ApplicationAsset, terminal1ApplicationAsset, terminal2ApplicationAsset);

    var cachedLicenceRefs = List.of("1", "2", "3");
    var applicationAssetLicenses = List.of(
        new ApplicationAssetLicence(null, null, null, cachedLicenceRefs.get(0)),
        new ApplicationAssetLicence(null, null, null, cachedLicenceRefs.get(1)),
        new ApplicationAssetLicence(null, null, null, cachedLicenceRefs.get(2))
    );

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY))).thenReturn(applicationAssets);
    when(applicationAssetLicenceService.getAssetLicences(applicationAssets)).thenReturn(applicationAssetLicenses);
    when(fieldService.findFieldsByIds(eq(fieldIds), anyString())).thenReturn(fields);
    when(terminalService.getTerminals(eq(terminalIds), anyString())).thenReturn(terminals);
    when(organisationUnitService.getOrganisationUnitsByIds(eq(List.of(field1ApplicationAsset.getAssetOperatorOuId())), anyString())).thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addAssetDetailsToContext(applicationVersion, builder);

    assertThat(builder.build())
        .extracting(
            ApplicationContext::primaryAsset,
            ApplicationContext::assetOperators,
            ApplicationContext::additionalFields,
            ApplicationContext::licences
        ).containsExactly(
            field1Json,
            Set.of(orgUnit1Json.name(), orgUnit2Json.name()),
            Collections.singleton(field2Json.getName()),
            new HashSet<>(cachedLicenceRefs)
        );
  }

  @Test
  void addPrimaryAsset_field() {
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    primaryAsset.setAssetType(AssetType.FIELD);
    primaryAsset.setAssetId(field1JsonWithOperatorAndLicences.getId());

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addPrimaryAsset(
        List.of(primaryAsset),
        List.of(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences),
        builder
    );

    assertThat(builder.build()).extracting(ApplicationContext::primaryAsset).isEqualTo(field1JsonWithOperatorAndLicences);
  }

  @Test
  void addPrimaryAsset_terminal() {
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    primaryAsset.setAssetType(AssetType.TERMINAL);
    primaryAsset.setAssetId(terminal1JsonWithOperator.getId());

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addPrimaryAsset(
        List.of(primaryAsset),
        List.of(terminal2JsonWithOperator, terminal1JsonWithOperator),
        builder
    );

    assertThat(builder.build()).extracting(ApplicationContext::primaryAsset).isEqualTo(terminal1JsonWithOperator);
  }

  @Test
  void addPrimaryAsset_noPrimaryAsset() {
    var applicationAssets = Collections.<ApplicationAsset>emptyList();
    var assetJsonList = Collections.<AssetJson>emptyList();
    var builder = ApplicationContext.newBuilder();

    assertThatThrownBy(() -> applicationContextService.addPrimaryAsset(applicationAssets, assetJsonList, builder))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void addAssetOperators() {
    var builder = ApplicationContext.newBuilder();

    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetOperatorOuId(1);

    when(organisationUnitService.getOrganisationUnitsByIds(eq(List.of(1)), anyString())).thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    var applicationAssets = List.of(applicationAsset);
    applicationContextService.addAssetOperators(applicationAssets, builder);

    var applicationContext = builder.build();
    var operators = applicationContext.assetOperators();
    assertThat(operators).containsExactly(orgUnit1Json.name(), orgUnit2Json.name());
  }

  @Test
  void addAdditionalFields() {
    var builder = ApplicationContext.newBuilder();
    applicationContextService.addAdditionalFields(
        List.of(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences),
        builder
    );

    var applicationContext = builder.build();
    var additionalFields = applicationContext.additionalFields();
    assertThat(additionalFields).containsExactly(
        field1JsonWithOperatorAndLicences.getName(),
        field2JsonWithOperatorAndLicences.getName()
    );
  }

  @Test
  void addLicences() {
    var cachedLicenceRefs = List.of("1", "2", "3");
    var applicationAssetLicenses = List.of(
        new ApplicationAssetLicence(null, null, null, cachedLicenceRefs.get(0)),
        new ApplicationAssetLicence(null, null, null, cachedLicenceRefs.get(1)),
        new ApplicationAssetLicence(null, null, null, cachedLicenceRefs.get(2))
    );

    var applicationAssets = List.of(new ApplicationAsset());
    when(applicationAssetLicenceService.getAssetLicences(applicationAssets)).thenReturn(applicationAssetLicenses);

    var builder = ApplicationContext.newBuilder();
    applicationContextService.addLicences(applicationAssets, builder);

    var applicationContext = builder.build();
    var licences = applicationContext.licences();
    assertThat(licences).containsExactlyElementsOf(cachedLicenceRefs);
  }
}

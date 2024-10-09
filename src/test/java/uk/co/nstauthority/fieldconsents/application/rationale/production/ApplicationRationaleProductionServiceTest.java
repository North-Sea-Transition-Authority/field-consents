package uk.co.nstauthority.fieldconsents.application.rationale.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field2AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2Json;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFigures;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleRepository;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleProductionServiceTest {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Mock
  private ApplicationRationaleRepository repository;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationRationaleService applicationRationaleService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentDataService consentDataService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ConsentFigureUnitService consentFigureUnitService;

  @Mock
  private ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService;

  private ApplicationRationaleProductionService applicationRationaleProductionService;

  @Captor
  private ArgumentCaptor<ApplicationRationale> applicationRationaleCaptor;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationale;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationRationale = new ApplicationRationale();
    applicationRationale.setApplicationVersion(applicationVersion);

    applicationRationaleProductionService = spy(new ApplicationRationaleProductionService(
        repository,
        applicationAssetService,
        applicationRationaleService,
        applicationVersionService,
        clock,
        consentDataService,
        consentLengthService,
        consentFigureUnitService,
        consentDataLongTermProductionFiguresService
    ));
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void saveApplicationRationale(ApplicationRationaleType rationaleType) {
    var comment = "comment";
    var assetKeys = List.of(field1AssetJson.getAssetKey(), field2AssetJson.getAssetKey(), terminal1AssetJson.getAssetKey());
    var hostAssetKey = assetKeys.getFirst();

    applicationRationaleProductionService.saveApplicationRationale(
        applicationVersion,
        rationaleType,
        comment,
        assetKeys,
        hostAssetKey
    );

    verify(repository).save(applicationRationaleCaptor.capture());
    assertThat(applicationRationaleCaptor.getValue())
        .extracting(
            ApplicationRationale::getApplicationVersion,
            ApplicationRationale::getRationaleType,
            ApplicationRationale::getComment
        ).containsExactly(
            applicationVersion,
            rationaleType,
            comment
        );

    verify(applicationAssetService).deleteAssetsByApplicationVersionAndAssetRoles(
        applicationVersion,
        Set.of(AssetRole.HOST, AssetRole.LOCATION)
    );

    for (var assetKey : assetKeys) {
      verify(applicationAssetService).createAssetForApplicationVersion(
          applicationVersion,
          assetKey,
          AssetRole.LOCATION
      );
    }

    verify(applicationAssetService).createAssetForApplicationVersion(
        applicationVersion,
        hostAssetKey,
        AssetRole.HOST
    );
  }

  @Test
  void getSummaryCard_rationaleNotFound() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    assertThat(applicationRationaleProductionService.getSummaryCard(applicationVersion)).isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getSummaryCard_increase_withComment() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.INCREASE);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(
            tuple("Is this application for an increase, decrease, extension or other?", ApplicationRationaleType.INCREASE.getDisplayName()),
            tuple("Why are you asking for an increase?", "comment")
        );
  }

  @Test
  void getSummaryCard_decrease_withComment() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.DECREASE);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(
            tuple("Is this application for an increase, decrease, extension or other?", ApplicationRationaleType.DECREASE.getDisplayName()),
            tuple("Why are you asking for a decrease?", "comment")
        );
  }

  @Test
  void getSummaryCard_extension_withComment() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.EXTENSION);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(
            tuple("Is this application for an increase, decrease, extension or other?", ApplicationRationaleType.EXTENSION.getDisplayName()),
            tuple("Why are you asking for an extension?", "comment")
        );
  }

  @Test
  void getSummaryCard_other_withComment() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.OTHER);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(
            tuple("Is this application for an increase, decrease, extension or other?", ApplicationRationaleType.OTHER.getDisplayName()),
            tuple("Why have you selected 'other'?", "comment")
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationRationaleType.class, names = "INCREASE", mode = EXCLUDE)
  void getSummaryCard_nonIncrease_withComment(ApplicationRationaleType applicationRationaleType) {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(applicationRationaleType);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("Is this application for an increase, decrease, extension or other?", applicationRationaleType.getDisplayName()));
  }

  @Test
  void getSummaryCard_nonHostLocations() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.NO_CHANGE);

    var nonHostLocations = List.of(field1Json, field2Json, terminal1Json, terminal2Json);
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(nonHostLocations);

    var expectedLocationsString = nonHostLocations.stream().map(assetJson -> ApplicationAssetView.from(assetJson).getName()).collect(
        Collectors.joining(", "));
    getSummaryKeyValuesFrom(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("At which location are the production activities?", expectedLocationsString));
  }

  @Test
  void getSummaryCard_onlyProductionLocationData() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var locations = List.of(field1Json, field2Json, terminal1Json, terminal2Json);
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(locations);

    var expectedLocationsString = "%s, %s, %s, %s".formatted(
        field1Json.getName(), field2Json.getName(), terminal1Json.getName(), terminal2Json.getName());

    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.emptyList());

    assertThat(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.simpleSummaryCard(
                SummaryDataView.newWithKeyValue("At which location are the production activities?", expectedLocationsString)
            )
        );
  }

  @Test
  void getSummaryCard_hostLocation() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.NO_CHANGE);

    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.singletonList(field1Json));

    getSummaryKeyValuesFrom(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("What is the host?", field1Json.getSelectionText()));
  }

  @Test
  void getSummaryCard_onlyHostLocationData() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.singletonList(field1Json));

    assertThat(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.simpleSummaryCard(
                SummaryDataView.newWithKeyValue("What is the host?", field1Json.getSelectionText())
            )
        );
  }

  @Test
  void getSummaryCard_noData() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.emptyList());

    assertThat(applicationRationaleProductionService.getSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void findOilAndGasMaximums() {
    var today = LocalDate.now(clock);
    var yesterday = today.minusDays(1);
    var lastWeek = today.minusWeeks(1);

    var currentYear = today.getYear();

    // this is the applicable one because it's starts last
    var applicableConsentData = ConsentDataTestUtil.newBuilder().withId(3).withConsentStartDate(today).build();

    var consentDataList = List.of(
        ConsentDataTestUtil.newBuilder().withId(2).withConsentStartDate(yesterday).build(),
        applicableConsentData,
        ConsentDataTestUtil.newBuilder().withId(1).withConsentStartDate(lastWeek).build()
    );

    var oilAndGasMaximums = new OilAndGasMaximums(
        currentYear,
        BigDecimal.valueOf(10),
        ProductionUnit.KSCM_PER_DAY,
        BigDecimal.valueOf(15),
        ProductionUnit.KSCM_PER_DAY
    );

    when(consentDataService.getConsentDataForYearAndApplicationVersionPrimaryAssetAndApplicationType(
        currentYear,
        applicationVersion
    )).thenReturn(consentDataList);

    doReturn(oilAndGasMaximums)
        .when(applicationRationaleProductionService)
        .getOilAndGasMaximumsForCurrentYear(currentYear, applicableConsentData);

    assertThat(applicationRationaleProductionService.findOilAndGasMaximums(applicationVersion)).contains(oilAndGasMaximums);
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = {"SHORT_TERM", "ANNUAL"}, mode = INCLUDE)
  void getOilAndGasMaximumsForCurrentYear_production_shortTerm_annual(ConsentLengthType consentLengthType) {
    var application = applicationVersion.getApplication();
    application.setType(ApplicationType.PRODUCTION);

    var today = LocalDate.now(clock);
    var currentYear = today.getYear();

    var consentData = ConsentDataTestUtil.newBuilder()
        .withApplication(application)
        .withShortTermOrAnnualProductionMaxOil(BigDecimal.ONE)
        .withShortTermOrAnnualProductionMaxGas(BigDecimal.TEN)
        .build();

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var consentFigureUnitView = new ConsentFigureUnitView(
        ProductionUnit.KSCM_PER_DAY,
        ProductionUnit.KSCM_PER_MONTH,
        FlareVentUnit.TONNES_PER_MONTH
    );

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthDetails.getConsentLength()))
        .thenReturn(consentFigureUnitView);

    assertThat(applicationRationaleProductionService.getOilAndGasMaximumsForCurrentYear(currentYear, consentData))
        .isEqualTo(new OilAndGasMaximums(
            currentYear,
            consentData.getShortTermOrAnnualProductionMaxOil(),
            consentFigureUnitView.productionOilUnit(),
            consentData.getShortTermOrAnnualProductionMaxGas(),
            consentFigureUnitView.productionGasUnit()
        ));
  }

  @Test
  void getOilAndGasMaximumsForCurrentYear_production_longTerm() {
    var application = applicationVersion.getApplication();
    application.setType(ApplicationType.PRODUCTION);

    var today = LocalDate.now(clock);
    var currentYear = today.getYear();

    var consentData = ConsentDataTestUtil.newBuilder()
        .withApplication(application)
        .withShortTermOrAnnualProductionMaxOil(BigDecimal.ONE)
        .withShortTermOrAnnualProductionMaxGas(BigDecimal.TEN)
        .build();

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    var consentFigureUnitView = new ConsentFigureUnitView(
        ProductionUnit.KSCM_PER_DAY,
        ProductionUnit.KSCM_PER_MONTH,
        FlareVentUnit.TONNES_PER_MONTH
    );

    var consentDataLongTermProductionFiguresPrevious = new ConsentDataLongTermProductionFigures();
    consentDataLongTermProductionFiguresPrevious.setYear(currentYear - 1);

    var consentDataLongTermProductionFiguresCurrent = new ConsentDataLongTermProductionFigures();
    consentDataLongTermProductionFiguresCurrent.setYear(currentYear);
    consentDataLongTermProductionFiguresCurrent.setMaxOil(BigDecimal.ONE);
    consentDataLongTermProductionFiguresCurrent.setMaxGas(BigDecimal.TEN);

    var consentDataLongTermProductionFiguresNext = new ConsentDataLongTermProductionFigures();
    consentDataLongTermProductionFiguresNext.setYear(currentYear + 1);

    var consentDataLongTermProductionFigures = List.of(
        consentDataLongTermProductionFiguresPrevious,
        consentDataLongTermProductionFiguresCurrent,
        consentDataLongTermProductionFiguresNext
    );

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthDetails.getConsentLength()))
        .thenReturn(consentFigureUnitView);

    when(consentDataLongTermProductionFiguresService.getConsentDataLongTermProductionFiguresList(application))
        .thenReturn(consentDataLongTermProductionFigures);

    assertThat(applicationRationaleProductionService.getOilAndGasMaximumsForCurrentYear(currentYear, consentData))
        .isEqualTo(new OilAndGasMaximums(
            currentYear,
            consentDataLongTermProductionFiguresCurrent.getMaxOil(),
            consentFigureUnitView.productionOilUnit(),
            consentDataLongTermProductionFiguresCurrent.getMaxGas(),
            consentFigureUnitView.productionGasUnit()
        ));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EXCLUDE)
  void getOilAndGasMaximumsForCurrentYear_nonProductionApplicationOnConsentData(ApplicationType applicationType) {
    var application = applicationVersion.getApplication();
    application.setType(applicationType);

    var today = LocalDate.now(clock);
    var currentYear = today.getYear();
    var consentData = ConsentDataTestUtil.newBuilder().withApplication(application).build();

    assertThatThrownBy(() -> applicationRationaleProductionService.getOilAndGasMaximumsForCurrentYear(currentYear, consentData))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Consent data [%s] is not for a production application. The application [%s] is of type %s"
            .formatted(consentData.getId(), application.getId(), applicationType));
  }

  private ListAssert<SummaryKeyValue> getSummaryKeyValuesFrom(SummaryCard summaryCard) {
    return assertThat(summaryCard)
        .extracting(SummaryCard::summaryData)
        .asInstanceOf(type(SummaryDataView.class))
        .extracting(SummaryDataView::keyValues)
        .asInstanceOf(list(SummaryKeyValue.class));
  }

}

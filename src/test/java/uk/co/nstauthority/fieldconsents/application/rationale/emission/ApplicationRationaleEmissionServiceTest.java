package uk.co.nstauthority.fieldconsents.application.rationale.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleEmissionServiceTest {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentDataService consentDataService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ConsentFigureUnitService consentFigureUnitService;

  private ApplicationRationaleEmissionService applicationRationaleEmissionService;

  @BeforeEach
  void setUp() {
    applicationRationaleEmissionService = spy(new ApplicationRationaleEmissionService(
        clock,
        applicationVersionService,
        consentDataService,
        consentLengthService,
        consentFigureUnitService
    ));
  }

  @Test
  void findEmissionDailyAverage() {
    var applicationVersion = new ApplicationVersion();
    var today = LocalDate.now(clock);
    var currentYear = today.getYear();

    var applicableConsentData = ConsentDataTestUtil.newBuilder().withConsentStartDate(today).build();

    var consentDataList = List.of(
        ConsentDataTestUtil.newBuilder().withConsentStartDate(today.minusDays(1)).build(),
        applicableConsentData,
        ConsentDataTestUtil.newBuilder().withConsentStartDate(today.minusDays(2)).build()
    );

    var emissionDailyAverage = new EmissionDailyAverage(
        applicableConsentData.getApplication().getType(),
        currentYear,
        BigDecimal.ONE,
        FlareVentUnit.TONNES_PER_MONTH
    );

    when(consentDataService.getConsentDataForYearAndApplicationVersionPrimaryAssetAndApplicationType(currentYear, applicationVersion))
        .thenReturn(consentDataList);

    doReturn(emissionDailyAverage)
        .when(applicationRationaleEmissionService)
        .getEmissionDailyAverage(currentYear, applicableConsentData);

    assertThat(applicationRationaleEmissionService.findEmissionDailyAverage(applicationVersion))
        .contains(emissionDailyAverage);
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = {"SHORT_TERM", "ANNUAL"}, mode = INCLUDE)
  void getEmissionDailyAverage_flare_shortTerm_annual(ConsentLengthType consentLengthType) {
    var applicationVersion = new ApplicationVersion();
    var application = new Application(1);
    application.setType(ApplicationType.FLARE);

    applicationVersion.setApplication(application);

    var today = LocalDate.now(clock);
    var currentYear = today.getYear();

    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(BigDecimal.ONE)
        .withApplication(application)
        .build();

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var consentFigureUnitView = new ConsentFigureUnitView(
        ProductionUnit.KSCM_PER_DAY,
        ProductionUnit.KSCM_PER_MONTH,
        FlareVentUnit.TONNES_PER_MONTH
    );

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType)).thenReturn(consentFigureUnitView);

    assertThat(applicationRationaleEmissionService.getEmissionDailyAverage(currentYear, consentData))
        .isEqualTo(new EmissionDailyAverage(
            consentData.getApplication().getType(),
            currentYear,
            consentData.getEmissionDailyAverage(),
            consentFigureUnitView.emissionAverageUnit()
        ));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"}, mode = EXCLUDE)
  void getEmissionDailyAverage_nonEmissionsApplicationOnConsentData(ApplicationType applicationType) {
    var applicationVersion = new ApplicationVersion();
    var application = new Application(1);
    application.setType(applicationType);

    applicationVersion.setApplication(application);

    var today = LocalDate.now(clock);
    var currentYear = today.getYear();

    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(BigDecimal.ONE)
        .withApplication(application)
        .build();

    assertThatThrownBy(() -> applicationRationaleEmissionService.getEmissionDailyAverage(currentYear, consentData))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Consent data [%s] is not for a flare/vent application. The application [%s] is of type %s"
            .formatted(consentData.getId(), application.getId(), applicationType));
  }

  @Test
  void getEmissionDailyAverage_longTerm() {
    var applicationVersion = new ApplicationVersion();
    var application = new Application(1);
    application.setType(ApplicationType.FLARE);

    applicationVersion.setApplication(application);

    var today = LocalDate.now(clock);
    var currentYear = today.getYear();

    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(BigDecimal.ONE)
        .withApplication(application)
        .build();

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThatThrownBy(() -> applicationRationaleEmissionService.getEmissionDailyAverage(currentYear, consentData))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Emissions daily average not applicable to long term application %s"
            .formatted(application.getId()));
  }
}

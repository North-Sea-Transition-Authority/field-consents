package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportGasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportTestUtil;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTestUtil;

@ExtendWith(MockitoExtension.class)
class VentReportGasDataServiceTest {

  @Mock
  private VentReportGasDataRepository ventReportGasDataRepository;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private VentReportPeriodService ventReportPeriodService;

  @Mock
  private EmissionReportGasDataSummaryService emissionReportGasDataSummaryService;

  private VentReportGasDataService ventReportGasDataService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    ventReportGasDataService = new VentReportGasDataService(ventReportGasDataRepository, applicationUnitService,
        ventReportPeriodService, emissionReportGasDataSummaryService);
  }

  @Test
  void findVentReportGasData_whenNoDataFound() {
    when(ventReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    assertThat(ventReportGasDataService.findVentReportGasData(applicationVersion)).isNotPresent();
  }

  @Test
  void findVentReportGasData_whenDataFound() {
    VentReportGasData reportGasData = FlareVentReportGasTestUtil.getCompleteAndValidVentReportGasData();
    when(ventReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(reportGasData));

    assertThat(ventReportGasDataService.findVentReportGasData(applicationVersion)).isPresent();
  }

  @Test
  void getFlareVentReportGasDataForm_withNoPreviousSavedData() {
    when(ventReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    FlareVentReportGasDataForm form = ventReportGasDataService.getFlareVentReportGasDataForm(applicationVersion);

    assertThat(form)
        .usingRecursiveComparison()
        .isEqualTo(new FlareVentReportGasDataForm());
  }

  @Test
  void getFlareVentReportGasDataForm_withExistingData() {
    VentReportGasData reportGasData = FlareVentReportGasTestUtil.getCompleteAndValidVentReportGasData();

    when(ventReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(reportGasData));

    FlareVentReportGasDataForm form = ventReportGasDataService.getFlareVentReportGasDataForm(applicationVersion);

    assertThat(form)
        .usingRecursiveComparison()
        .isEqualTo(FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm());
  }

  @Test
  void saveVentReportGasData_withEachCategoryEvaluatedIndividually() {
    FlareVentReportGasDataForm form = FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm();

    ventReportGasDataService.saveVentReportGasData(applicationVersion, form);

    verify(ventReportGasDataRepository, times(1)).deleteByApplicationVersion(applicationVersion);

    ArgumentCaptor<VentReportGasData> gasDataArgumentCaptor = ArgumentCaptor.forClass(VentReportGasData.class);
    verify(ventReportGasDataRepository, times(1)).save(gasDataArgumentCaptor.capture());

    VentReportGasData savedReportGasData = gasDataArgumentCaptor.getValue();

    FlareVentReportGasTestUtil.verifyCategoryData(form, savedReportGasData);

    assertThat(savedReportGasData.getEvaluatedPerCategory()).isTrue();
    assertThat(savedReportGasData.getEvaluatedPerCategoryExplanation()).isNull();
  }

  @Test
  void saveVentReportGasData_whereEachCategoryNotEvaluatedIndividually() {
    FlareVentReportGasDataForm form = FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm();
    form.setEvaluatedPerCategory(false);
    form.setEvaluatedPerCategoryExplanation("explanation test");

    ventReportGasDataService.saveVentReportGasData(applicationVersion, form);

    verify(ventReportGasDataRepository, times(1)).deleteByApplicationVersion(applicationVersion);

    ArgumentCaptor<VentReportGasData> gasDataArgumentCaptor = ArgumentCaptor.forClass(VentReportGasData.class);
    verify(ventReportGasDataRepository, times(1)).save(gasDataArgumentCaptor.capture());

    VentReportGasData savedReportGasData = gasDataArgumentCaptor.getValue();

    FlareVentReportGasTestUtil.verifyCategoryData(form, savedReportGasData);

    assertThat(savedReportGasData.getEvaluatedPerCategory()).isFalse();
    assertThat(savedReportGasData.getEvaluatedPerCategoryExplanation()).isEqualTo("explanation test");
  }

  @Test
  void getVentReportGasDataSummaryCards_nonDataExists() {
    when(ventReportGasDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(ventReportGasDataService.getVentReportGasDataSummaryCards(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCardList());
  }

  @Test
  void getVentReportGasDataSummaryCards_dataExists() {
    var reportGasData = FlareVentReportGasTestUtil.getCompleteAndValidVentReportGasData();
    var ventReportPeriod = VentReportTestUtil.getFullVentReportPeriod();
    var ventGasDensityUnit = FlareVentUnit.KG_PER_CUBIC_METER;
    var ventGasContentUnit = FlareVentUnit.MASS_PERCENTAGE;
    var tableSummaryCard = SummaryTestUtil.getTableSummaryCard();
    var simpleSummaryCard = SummaryTestUtil.getSimpleSummaryCard();

    when(ventReportGasDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(reportGasData));
    when(applicationUnitService.getVentGasDensityUnit(applicationVersion))
        .thenReturn(ventGasDensityUnit);
    when(applicationUnitService.getVentGasContentUnit(applicationVersion))
        .thenReturn(ventGasContentUnit);
    when(ventReportPeriodService.getVentReportPeriodOrError(applicationVersion))
        .thenReturn(ventReportPeriod);
    when(emissionReportGasDataSummaryService.getReportGasDataTableSummaryCard(
        reportGasData, ventReportPeriod, ventGasDensityUnit, ventGasContentUnit))
        .thenReturn(tableSummaryCard);
    when(emissionReportGasDataSummaryService.getReportGasDataJustificationSummaryCard(reportGasData))
        .thenReturn(simpleSummaryCard);

    assertThat(ventReportGasDataService.getVentReportGasDataSummaryCards(applicationVersion))
        .isEqualTo(List.of(tableSummaryCard, simpleSummaryCard));
  }
}
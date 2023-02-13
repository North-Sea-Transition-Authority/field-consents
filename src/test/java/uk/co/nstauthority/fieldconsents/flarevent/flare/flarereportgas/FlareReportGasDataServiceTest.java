package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil;

@ExtendWith(MockitoExtension.class)
class FlareReportGasDataServiceTest {

  @Mock
  private FlareReportGasDataRepository flareReportGasDataRepository;

  private FlareReportGasDataService flareReportGasDataService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    flareReportGasDataService = new FlareReportGasDataService(flareReportGasDataRepository);
  }

  @Test
  void findFlareReportGasData_whenNoDataFound() {
    when(flareReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    assertThat(flareReportGasDataService.findFlareReportGasData(applicationVersion)).isNotPresent();
  }

  @Test
  void findFlareReportGasData_whenDataFound() {
    FlareReportGasData reportGasData = FlareVentReportGasTestUtil.getCompleteAndValidFlareReportGasData();
    when(flareReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(reportGasData));

    assertThat(flareReportGasDataService.findFlareReportGasData(applicationVersion)).isPresent();
  }

  @Test
  void getFlareVentReportGasDataForm_withNoPreviousSavedData() {
    when(flareReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    FlareVentReportGasDataForm form = flareReportGasDataService.getFlareVentReportGasDataForm(applicationVersion);

    assertThat(form)
        .usingRecursiveComparison()
        .isEqualTo(new FlareVentReportGasDataForm());
  }

  @Test
  void getFlareVentReportGasDataForm_withExistingData() {
    FlareReportGasData reportGasData = FlareVentReportGasTestUtil.getCompleteAndValidFlareReportGasData();

    when(flareReportGasDataRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(reportGasData));

    FlareVentReportGasDataForm form = flareReportGasDataService.getFlareVentReportGasDataForm(applicationVersion);

    assertThat(form)
        .usingRecursiveComparison()
        .isEqualTo(FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm());
  }

  @Test
  void saveFlareReportGasData_withEachCategoryEvaluatedIndividually() {
    FlareVentReportGasDataForm form = FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm();

    flareReportGasDataService.saveFlareReportGasData(applicationVersion, form);

    verify(flareReportGasDataRepository, times(1)).deleteByApplicationVersion(applicationVersion);

    ArgumentCaptor<FlareReportGasData> gasDataArgumentCaptor = ArgumentCaptor.forClass(FlareReportGasData.class);
    verify(flareReportGasDataRepository, times(1)).save(gasDataArgumentCaptor.capture());

    FlareReportGasData savedReportGasData = gasDataArgumentCaptor.getValue();

    FlareVentReportGasTestUtil.verifyCategoryData(form, savedReportGasData);

    assertThat(savedReportGasData.getEvaluatedPerCategory()).isTrue();
    assertThat(savedReportGasData.getEvaluatedPerCategoryExplanation()).isNull();
  }

  @Test
  void saveFlareReportGasData_whereEachCategoryNotEvaluatedIndividually() {
    FlareVentReportGasDataForm form = FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm();
    form.setEvaluatedPerCategory(false);
    form.setEvaluatedPerCategoryExplanation("explanation test");

    flareReportGasDataService.saveFlareReportGasData(applicationVersion, form);

    verify(flareReportGasDataRepository, times(1)).deleteByApplicationVersion(applicationVersion);

    ArgumentCaptor<FlareReportGasData> gasDataArgumentCaptor = ArgumentCaptor.forClass(FlareReportGasData.class);
    verify(flareReportGasDataRepository, times(1)).save(gasDataArgumentCaptor.capture());

    FlareReportGasData savedReportGasData = gasDataArgumentCaptor.getValue();

    FlareVentReportGasTestUtil.verifyCategoryData(form, savedReportGasData);

    assertThat(savedReportGasData.getEvaluatedPerCategory()).isFalse();
    assertThat(savedReportGasData.getEvaluatedPerCategoryExplanation()).isEqualTo("explanation test");
  }

}
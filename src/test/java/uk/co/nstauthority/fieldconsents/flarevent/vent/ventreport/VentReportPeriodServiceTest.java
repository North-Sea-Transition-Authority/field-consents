package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Month;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

@ExtendWith(MockitoExtension.class)
class VentReportPeriodServiceTest {

  @Mock
  private VentReportPeriodRepository ventReportPeriodRepository;

  @Mock
  private VentReportCleanupService ventReportCleanupService;

  private VentReportPeriodService ventReportPeriodService;

  private ApplicationVersion applicationVersion;

  private String exceptionMessage;

  private VentReportPeriod ventReportPeriod;

  @BeforeEach
  void setUp() {
    ventReportPeriodService = new VentReportPeriodService(ventReportPeriodRepository, ventReportCleanupService);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    exceptionMessage = "Vent report period with application_version_id %s not found".formatted(applicationVersion.getId());
    ventReportPeriod = new VentReportPeriod(applicationVersion, Month.APRIL, 2023);
  }

  @Test
  void findVentReportPeriod_exists() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(ventReportPeriod));

    var ventReportPeriodOptional = ventReportPeriodService.findVentReportPeriod(applicationVersion);

    assertThat(ventReportPeriodOptional).isNotEmpty();

    assertThat(ventReportPeriodOptional.get())
        .extracting(
            VentReportPeriod::getApplicationVersion,
            VentReportPeriod::getReportEndMonth,
            VentReportPeriod::getReportEndYear)
        .containsExactly(applicationVersion, Month.APRIL, 2023);
  }

  @Test
  void findVentReportPeriod_notExists() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    var ventReportPeriodOptional = ventReportPeriodService.findVentReportPeriod(applicationVersion);

    assertThat(ventReportPeriodOptional).isEmpty();
  }

  @Test
  void getVentReportPeriodForm_newForm() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    var ventReportPeriodForm = ventReportPeriodService.getVentReportPeriodForm(applicationVersion);

    assertThat(ventReportPeriodForm)
        .extracting(
            form -> form.getReportEndMonth().getInputValue(),
            form -> form.getReportEndYear().getInputValue())
        .containsExactly(null, null);
  }

  @Test
  void getVentReportPeriodForm_populateFormFromDbData() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(ventReportPeriod));

    var ventReportPeriodForm = ventReportPeriodService.getVentReportPeriodForm(applicationVersion);

    assertThat(ventReportPeriodForm)
        .extracting(
            form -> form.getReportEndMonth().getInputValue(),
            form -> form.getReportEndYear().getInputValue())
        .containsExactly("APRIL", "2023");
  }

  @Test
  void ventReportPeriodExists_false() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    assertThat(ventReportPeriodService.ventReportPeriodExists(applicationVersion)).isFalse();
  }

  @Test
  void ventReportPeriodExists_true() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(ventReportPeriod));

    assertThat(ventReportPeriodService.ventReportPeriodExists(applicationVersion)).isTrue();
  }

  @Test
  void getVentReportPeriodOrError_exists() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(ventReportPeriod));

    var ventReportPeriod = ventReportPeriodService.getVentReportPeriodOrError(applicationVersion);

    assertThat(ventReportPeriod)
        .extracting(
            VentReportPeriod::getApplicationVersion,
            VentReportPeriod::getReportEndMonth,
            VentReportPeriod::getReportEndYear)
        .containsExactly(applicationVersion, Month.APRIL, 2023);
  }

  @Test
  void getVentReportPeriodOrError_notExists() {
    when(ventReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    assertThatThrownBy(() -> ventReportPeriodService.getVentReportPeriodOrError(applicationVersion))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(exceptionMessage);
  }

  @Test
  void saveVentReportPeriod() {
    FlareVentReportPeriodForm reportPeriodForm = VentReportTestUtil.getFullVentReportPeriodForm();

    ventReportPeriodService.saveVentReportPeriod(applicationVersion, reportPeriodForm);

    verify(ventReportPeriodRepository, times(1)).deleteByApplicationVersion(applicationVersion);

    ArgumentCaptor<VentReportPeriod> ventReportPeriodArgumentCaptor = ArgumentCaptor.forClass(
        VentReportPeriod.class);
    verify(ventReportPeriodRepository, times(1)).save(ventReportPeriodArgumentCaptor.capture());
    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    verify(ventReportCleanupService, times(1)).removeObsoleteReportDataOnPeriodSave(
        applicationVersionArgumentCaptor.capture(),
        ventReportPeriodArgumentCaptor.capture()
    );

    assertThat(applicationVersionArgumentCaptor.getValue()).isEqualTo(applicationVersion);

    assertThat(ventReportPeriodArgumentCaptor.getValue())
        .extracting(VentReportPeriod::getApplicationVersion,
            VentReportPeriod::getReportEndYear,
            VentReportPeriod::getReportEndMonth)
        .containsExactly(applicationVersion, 2023, Month.APRIL);
  }
}

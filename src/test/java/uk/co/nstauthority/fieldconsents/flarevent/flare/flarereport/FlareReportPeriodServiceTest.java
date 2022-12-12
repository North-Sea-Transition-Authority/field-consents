package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class FlareReportPeriodServiceTest {

  @Mock
  private FlareReportPeriodRepository flareReportPeriodRepository;

  private FlareReportPeriodService flareReportPeriodService;

  private ApplicationVersion applicationVersion;

  private String exceptionMessage;

  @BeforeEach
  void setUp() {
    flareReportPeriodService = new FlareReportPeriodService(flareReportPeriodRepository);
    applicationVersion = FlareReportTestUtil.flareAppVersion;
    exceptionMessage = "Flare report period with application_version_id %s not found".formatted(applicationVersion.getId());
  }

  @Test
  void findFlareReportPeriod_exists() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(FlareReportTestUtil.getFullFlareReportPeriod()));

    var flareReportPeriodOptional = flareReportPeriodService.findFlareReportPeriod(applicationVersion);

    assertThat(flareReportPeriodOptional).isNotEmpty();

    assertThat(flareReportPeriodOptional.get())
        .extracting(
            FlareReportPeriod::getApplicationVersion,
            FlareReportPeriod::getReportEndMonth,
            FlareReportPeriod::getReportEndYear)
        .containsExactly(applicationVersion, Month.APRIL, 2023);
  }

  @Test
  void findFlareReportPeriod_notExists() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    var flareReportPeriodOptional = flareReportPeriodService.findFlareReportPeriod(applicationVersion);

    assertThat(flareReportPeriodOptional).isEmpty();
  }

  @Test
  void getFlareReportPeriodForm_newForm() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    var flareReportPeriodForm = flareReportPeriodService.getFlareReportPeriodForm(applicationVersion);

    assertThat(flareReportPeriodForm)
        .extracting(
            form -> form.getReportEndMonth().getInputValue(),
            form -> form.getReportEndYear().getInputValue())
        .containsExactly(null, null);
  }

  @Test
  void getFlareReportPeriodForm_populateFormFromDbData() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(FlareReportTestUtil.getFullFlareReportPeriod()));

    var flareReportPeriodForm = flareReportPeriodService.getFlareReportPeriodForm(applicationVersion);

    assertThat(flareReportPeriodForm)
        .extracting(
            form -> form.getReportEndMonth().getInputValue(),
            form -> form.getReportEndYear().getInputValue())
        .containsExactly("APRIL", "2023");
  }

  @Test
  void flareReportPeriodExists_false() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    assertThat(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).isFalse();
  }

  @Test
  void flareReportPeriodExists_true() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(FlareReportTestUtil.getFullFlareReportPeriod()));

    assertThat(flareReportPeriodService.flareReportPeriodExists(applicationVersion)).isTrue();
  }

  @Test
  void getFlareReportPeriodOrError_exists() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.of(FlareReportTestUtil.getFullFlareReportPeriod()));

    var flareReportPeriod = flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion);

    assertThat(flareReportPeriod)
        .extracting(
            FlareReportPeriod::getApplicationVersion,
            FlareReportPeriod::getReportEndMonth,
            FlareReportPeriod::getReportEndYear)
        .containsExactly(applicationVersion, Month.APRIL, 2023);
  }

  @Test
  void getFlareReportPeriodOrError_notExists() {
    when(flareReportPeriodRepository.findByApplicationVersion(applicationVersion)).
        thenReturn(Optional.empty());

    assertThatThrownBy(() -> flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(exceptionMessage);
  }

  @Test
  void saveFlareReportPeriod() {
    FlareReportPeriodForm flareReportPeriodForm = FlareReportTestUtil.getFullFlareReportPeriodForm();

    flareReportPeriodService.saveFlareReportPeriod(applicationVersion, flareReportPeriodForm);

    verify(flareReportPeriodRepository, times(1)).deleteByApplicationVersion(applicationVersion);

    ArgumentCaptor<FlareReportPeriod> flareReportPeriodArgumentCaptor = ArgumentCaptor.forClass(FlareReportPeriod.class);
    verify(flareReportPeriodRepository, times(1)).save(flareReportPeriodArgumentCaptor.capture());
  }
}

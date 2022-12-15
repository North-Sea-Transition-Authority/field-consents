package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;

@ExtendWith(MockitoExtension.class)
class VentShortTermServiceTest {

  @Mock
  private VentShortTermMonthRepository ventShortTermMonthRepository;

  @Mock
  private ConsentLengthService consentLengthService;

  private VentShortTermService ventShortTermService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    ventShortTermService = new VentShortTermService(ventShortTermMonthRepository, consentLengthService);
    applicationVersion = VentShortTermTestUtil.ventAppVersion;
  }

  @Test
  void ventShortTermMonthsExist() {
    when(ventShortTermMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(true);

    assertThat(ventShortTermService.ventShortTermMonthsExist(applicationVersion)).isTrue();
  }

  @Test
  void ventShortTermMonthsExist_false() {
    when(ventShortTermMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(false);

    assertThat(ventShortTermService.ventShortTermMonthsExist(applicationVersion)).isFalse();
  }


  @Test
  void ventShortTermMonthsComplete_noShortTermMonths() {
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion));
    when(ventShortTermMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    assertThat(ventShortTermService.ventShortTermMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void ventShortTermMonthsComplete_ShortTermMonthsMissing() {
    List<VentShortTermMonth> ventShortTermMonths =
        VentShortTermTestUtil.getVentShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE,
            ConsentLengthTestUtil.SHORT_TERM_END_DATE);
    ventShortTermMonths.remove(0); // remove first month

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion));
    when(ventShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventShortTermMonths);

    assertThat(ventShortTermService.ventShortTermMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void ventShortTermComplete_ShortTermMonthsAlign() {
    List<VentShortTermMonth> ventShortTermMonths =
        VentShortTermTestUtil.getVentShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE,
            ConsentLengthTestUtil.SHORT_TERM_END_DATE);

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion));
    when(ventShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventShortTermMonths);

    assertThat(ventShortTermService.ventShortTermMonthsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getVentShortTermForm_noExistingData() {
    var consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    when(ventShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    VentShortTermForm ventShortTermForm = ventShortTermService.getVentShortTermForm(applicationVersion);

    assertThat(ventShortTermForm.getStartDate()).isEqualTo("31 Oct 2022");
    assertThat(ventShortTermForm.getEndDate()).isEqualTo("12 Apr 2023");

    assertOnlyStubData(ventShortTermForm.getVentShortTermMonthForms());
  }

  @Test
  void getVentShortTermForm_existingDataNotOverlapping() {
    var consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    List<VentShortTermMonth> ventShortTermMonths =
        VentShortTermTestUtil.getVentShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE.minusMonths(12),
            ConsentLengthTestUtil.SHORT_TERM_END_DATE.minusMonths(12));

    when(ventShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventShortTermMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    VentShortTermForm ventShortTermForm = ventShortTermService.getVentShortTermForm(applicationVersion);

    assertThat(ventShortTermForm.getStartDate()).isEqualTo("31 Oct 2022");
    assertThat(ventShortTermForm.getEndDate()).isEqualTo("12 Apr 2023");

    assertOnlyStubData(ventShortTermForm.getVentShortTermMonthForms());
  }

  @Test
  void getVentShortTermForm_existingDataOverlapping() {
    var consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    List<VentShortTermMonth> ventShortTermMonths =
        VentShortTermTestUtil.getVentShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE.minusMonths(2),
            ConsentLengthTestUtil.SHORT_TERM_END_DATE.minusMonths(2));

    when(ventShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventShortTermMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    VentShortTermForm ventShortTermForm = ventShortTermService.getVentShortTermForm(applicationVersion);

    assertThat(ventShortTermForm.getStartDate()).isEqualTo("31 Oct 2022");
    assertThat(ventShortTermForm.getEndDate()).isEqualTo("12 Apr 2023");

    assertThat(ventShortTermForm.getVentShortTermMonthForms())
        .extracting(VentShortTermMonthForm::getYear,
            VentShortTermMonthForm::getMonth,
            VentShortTermMonthForm::getStartDate,
            VentShortTermMonthForm::getEndDate,
            VentShortTermMonthForm::getConsentDays,
            form -> form.getComments().getInputValue(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getInputValue())
        .containsExactly(
            tuple("2022",
                getMonthDisplayName(Month.OCTOBER),
                ConsentLengthTestUtil.SHORT_TERM_START_DATE,
                LocalDate.of(2022, Month.OCTOBER, 31),
                1,
                null, null, null, null),
            tuple("2022",
                getMonthDisplayName(Month.NOVEMBER),
                LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30),
                30,
                "comment4", "4", "40", "400"),
            tuple("2022",
                getMonthDisplayName(Month.DECEMBER),
                LocalDate.of(2022, Month.DECEMBER, 1),
                LocalDate.of(2022, Month.DECEMBER, 31),
                31,
                "comment5", "5", "50", "500"),
            tuple("2023",
                getMonthDisplayName(Month.JANUARY),
                LocalDate.of(2023, Month.JANUARY, 1),
                LocalDate.of(2023, Month.JANUARY, 31),
                31,
                "comment6", "6", "60", "600"),
            tuple("2023",
                getMonthDisplayName(Month.FEBRUARY),
                LocalDate.of(2023, Month.FEBRUARY, 1),
                LocalDate.of(2023, Month.FEBRUARY, 28),
                28,
                null, null, null, null),
            tuple("2023",
                getMonthDisplayName(Month.MARCH),
                LocalDate.of(2023, Month.MARCH, 1),
                LocalDate.of(2023, Month.MARCH, 31),
                31,
                null, null, null, null),
            tuple("2023",
                getMonthDisplayName(Month.APRIL),
                LocalDate.of(2023, Month.APRIL, 1),
                ConsentLengthTestUtil.SHORT_TERM_END_DATE,
                12,
                null, null, null, null));
  }

  @Test
  void saveVentShortTerm() {
    ventShortTermService.saveVentShortTerm(applicationVersion,
        VentShortTermTestUtil.getFullVentShortTermFormForPeriod(
            ConsentLengthTestUtil.SHORT_TERM_START_DATE,
            ConsentLengthTestUtil.SHORT_TERM_END_DATE));

    verify(ventShortTermMonthRepository, times(1))
        .deleteAllByApplicationVersion(applicationVersion);

    ArgumentCaptor<VentShortTermMonth> ventShortTermMonthArgumentCaptor =
        ArgumentCaptor.forClass(VentShortTermMonth.class);
    verify(ventShortTermMonthRepository, times(7))
        .save(ventShortTermMonthArgumentCaptor.capture());
  }

  private String getMonthDisplayName(Month month) {
    return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
  }

  private void assertOnlyStubData(List<VentShortTermMonthForm> ventShortTermMonthForms) {

    assertThat(ventShortTermMonthForms)
        .extracting(VentShortTermMonthForm::getYear,
            VentShortTermMonthForm::getMonth,
            VentShortTermMonthForm::getStartDate,
            VentShortTermMonthForm::getEndDate,
            VentShortTermMonthForm::getConsentDays,
            form -> form.getComments().getInputValue(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getInputValue())
        .containsExactly(
            tuple("2022",
                getMonthDisplayName(Month.OCTOBER),
                ConsentLengthTestUtil.SHORT_TERM_START_DATE,
                LocalDate.of(2022, Month.OCTOBER, 31),
                1,
                null, null, null, null),
            tuple("2022",
                getMonthDisplayName(Month.NOVEMBER),
                LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30),
                30,
                null, null, null, null),
            tuple("2022",
                getMonthDisplayName(Month.DECEMBER),
                LocalDate.of(2022, Month.DECEMBER, 1),
                LocalDate.of(2022, Month.DECEMBER, 31),
                31,
                null, null, null, null),
            tuple("2023",
                getMonthDisplayName(Month.JANUARY),
                LocalDate.of(2023, Month.JANUARY, 1),
                LocalDate.of(2023, Month.JANUARY, 31),
                31,
                null, null, null, null),
            tuple("2023",
                getMonthDisplayName(Month.FEBRUARY),
                LocalDate.of(2023, Month.FEBRUARY, 1),
                LocalDate.of(2023, Month.FEBRUARY, 28),
                28,
                null, null, null, null),
            tuple("2023",
                getMonthDisplayName(Month.MARCH),
                LocalDate.of(2023, Month.MARCH, 1),
                LocalDate.of(2023, Month.MARCH, 31),
                31,
                null, null, null, null),
            tuple("2023",
                getMonthDisplayName(Month.APRIL),
                LocalDate.of(2023, Month.APRIL, 1),
                ConsentLengthTestUtil.SHORT_TERM_END_DATE,
                12,
                null, null, null, null));

  }

}

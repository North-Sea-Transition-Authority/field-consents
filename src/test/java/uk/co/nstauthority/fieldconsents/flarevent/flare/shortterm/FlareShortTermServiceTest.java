package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

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
class FlareShortTermServiceTest {

  @Mock
  private FlareShortTermMonthRepository flareShortTermMonthRepository;

  @Mock
  private ConsentLengthService consentLengthService;

  private FlareShortTermService flareShortTermService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    flareShortTermService = new FlareShortTermService(flareShortTermMonthRepository, consentLengthService);
    applicationVersion = FlareShortTermTestUtil.flareAppVersion;
  }

  @Test
  void flareShortTermMonthsExist() {
    when(flareShortTermMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(true);

    assertThat(flareShortTermService.flareShortTermMonthsExist(applicationVersion)).isTrue();
  }

  @Test
  void flareShortTermMonthsExist_false() {
    when(flareShortTermMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(false);

    assertThat(flareShortTermService.flareShortTermMonthsExist(applicationVersion)).isFalse();
  }


  @Test
  void flareShortTermMonthsComplete_noShortTermMonths() {
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion));
    when(flareShortTermMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    assertThat(flareShortTermService.flareShortTermMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void flareShortTermMonthsComplete_ShortTermMonthsMissing() {
    List<FlareShortTermMonth> flareShortTermMonths =
        FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE,
            ConsentLengthTestUtil.SHORT_TERM_END_DATE);
    flareShortTermMonths.remove(0); // remove first month

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion));
    when(flareShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareShortTermMonths);

    assertThat(flareShortTermService.flareShortTermMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void flareShortTermComplete_ShortTermMonthsAlign() {
    List<FlareShortTermMonth> flareShortTermMonths =
        FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE,
            ConsentLengthTestUtil.SHORT_TERM_END_DATE);

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion));
    when(flareShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareShortTermMonths);

    assertThat(flareShortTermService.flareShortTermMonthsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getFlareShortTermForm_noExistingData() {
    var consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    when(flareShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    FlareShortTermForm flareShortTermForm = flareShortTermService.getFlareShortTermForm(applicationVersion);

    assertThat(flareShortTermForm.getStartDate()).isEqualTo("31 Oct 2022");
    assertThat(flareShortTermForm.getEndDate()).isEqualTo("12 Apr 2023");

    assertOnlyStubData(flareShortTermForm.getFlareShortTermMonthForms());
  }

  @Test
  void getFlareShortTermForm_existingDataNotOverlapping() {
    var consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    List<FlareShortTermMonth> flareShortTermMonths =
        FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE.minusMonths(12),
            ConsentLengthTestUtil.SHORT_TERM_END_DATE.minusMonths(12));

    when(flareShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareShortTermMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    FlareShortTermForm flareShortTermForm = flareShortTermService.getFlareShortTermForm(applicationVersion);

    assertThat(flareShortTermForm.getStartDate()).isEqualTo("31 Oct 2022");
    assertThat(flareShortTermForm.getEndDate()).isEqualTo("12 Apr 2023");

    assertOnlyStubData(flareShortTermForm.getFlareShortTermMonthForms());
  }

  @Test
  void getFlareShortTermForm_existingDataOverlapping() {
    var consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    List<FlareShortTermMonth> flareShortTermMonths =
        FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE.minusMonths(2),
            ConsentLengthTestUtil.SHORT_TERM_END_DATE.minusMonths(2));

    when(flareShortTermMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareShortTermMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    FlareShortTermForm flareShortTermForm = flareShortTermService.getFlareShortTermForm(applicationVersion);

    assertThat(flareShortTermForm.getStartDate()).isEqualTo("31 Oct 2022");
    assertThat(flareShortTermForm.getEndDate()).isEqualTo("12 Apr 2023");

    assertThat(flareShortTermForm.getFlareShortTermMonthForms())
        .extracting(FlareShortTermMonthForm::getYear,
            FlareShortTermMonthForm::getMonth,
            FlareShortTermMonthForm::getStartDate,
            FlareShortTermMonthForm::getEndDate,
            FlareShortTermMonthForm::getConsentDays,
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
  void saveFlareShortTerm() {
    flareShortTermService.saveFlareShortTerm(applicationVersion,
        FlareShortTermTestUtil.getFullFlareShortTermFormForPeriod(
            ConsentLengthTestUtil.SHORT_TERM_START_DATE,
            ConsentLengthTestUtil.SHORT_TERM_END_DATE));

    verify(flareShortTermMonthRepository, times(1))
        .deleteAllByApplicationVersion(applicationVersion);

    ArgumentCaptor<FlareShortTermMonth> flareShortTermMonthArgumentCaptor =
        ArgumentCaptor.forClass(FlareShortTermMonth.class);
    verify(flareShortTermMonthRepository, times(7))
        .save(flareShortTermMonthArgumentCaptor.capture());
  }

  private String getMonthDisplayName(Month month) {
    return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
  }

  private void assertOnlyStubData(List<FlareShortTermMonthForm> flareShortTermMonthForms) {

    assertThat(flareShortTermMonthForms)
        .extracting(FlareShortTermMonthForm::getYear,
            FlareShortTermMonthForm::getMonth,
            FlareShortTermMonthForm::getStartDate,
            FlareShortTermMonthForm::getEndDate,
            FlareShortTermMonthForm::getConsentDays,
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

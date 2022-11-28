package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class FlareAnnualServiceTest {

  @Mock
  private FlareAnnualMonthRepository flareAnnualMonthRepository;

  @Mock
  private ConsentLengthService consentLengthService;

  private FlareAnnualService flareAnnualService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    flareAnnualService = new FlareAnnualService(flareAnnualMonthRepository, consentLengthService);
    applicationVersion = FlareAnnualTestUtil.flareAppVersion;
  }

  @Test
  void flareAnnualMonthsExist() {
    when(flareAnnualMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(true);

    assertThat(flareAnnualService.flareAnnualMonthsExist(applicationVersion)).isTrue();
  }

  @Test
  void flareAnnualMonthsExist_false() {
    when(flareAnnualMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(false);

    assertThat(flareAnnualService.flareAnnualMonthsExist(applicationVersion)).isFalse();
  }


  @Test
  void flareAnnualMonthsComplete_noAnnualMonths() {
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion));
    when(flareAnnualMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    assertThat(flareAnnualService.flareAnnualMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void flareAnnualMonthsComplete_annualMonthsMissing() {
    List<FlareAnnualMonth> flareAnnualMonths =
        FlareAnnualTestUtil.getFlareAnnualMonthsForYear(applicationVersion, ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);
    flareAnnualMonths.remove(0); // remove first month

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion));
    when(flareAnnualMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(flareAnnualMonths);

    assertThat(flareAnnualService.flareAnnualMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void flareAnnualComplete_annualMonthsAlign() {
    List<FlareAnnualMonth> flareAnnualMonths =
        FlareAnnualTestUtil.getFlareAnnualMonthsForYear(applicationVersion, ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion));
    when(flareAnnualMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(flareAnnualMonths);

    assertThat(flareAnnualService.flareAnnualMonthsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getFlareAnnualForm_noExistingData() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentYear = consentLengthDetails.getAnnualConsentYear();
    String consentYearString = String.valueOf(consentYear);

    when(flareAnnualMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    FlareAnnualForm flareAnnualForm = flareAnnualService.getFlareAnnualForm(applicationVersion);

    assertThat(flareAnnualForm.getYear()).isEqualTo(consentYearString);

    assertOnlyStubData(flareAnnualForm.getFlareAnnualMonthForms(), consentYearString);
  }

  @Test
  void getFlareAnnualForm_existingDataNotOverlapping() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentYear = consentLengthDetails.getAnnualConsentYear();
    String consentYearString = String.valueOf(consentYear);

    List<FlareAnnualMonth> flareAnnualMonths =
        FlareAnnualTestUtil.getFlareAnnualMonthsForYear(applicationVersion, consentYear - 1);

    when(flareAnnualMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareAnnualMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    FlareAnnualForm flareAnnualForm = flareAnnualService.getFlareAnnualForm(applicationVersion);

    assertThat(flareAnnualForm.getYear()).isEqualTo(consentYearString);

    assertOnlyStubData(flareAnnualForm.getFlareAnnualMonthForms(), consentYearString);
  }


  @Test
  void getFlareAnnualForm_existingDataOverlapping() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentYear = consentLengthDetails.getAnnualConsentYear();
    String consentYearString = String.valueOf(consentYear);

    List<FlareAnnualMonth> flareAnnualMonths =
        FlareAnnualTestUtil.getFlareAnnualMonthsForYear(applicationVersion, consentYear);

    when(flareAnnualMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareAnnualMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    FlareAnnualForm flareAnnualForm = flareAnnualService.getFlareAnnualForm(applicationVersion);

    assertThat(flareAnnualForm.getYear()).isEqualTo(consentYearString);

    assertThat(flareAnnualForm.getFlareAnnualMonthForms())
        .extracting(FlareAnnualMonthForm::getYear,
            FlareAnnualMonthForm::getMonth,
            form -> form.getComments().getInputValue(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getInputValue())
        .containsExactly(
            tuple(consentYearString,
                getMonthDisplayName(Month.JANUARY),
                "comment1", "1", "10", "100"),
            tuple(consentYearString,
                getMonthDisplayName(Month.FEBRUARY),
                "comment2", "2", "20", "200"),
            tuple(consentYearString,
                getMonthDisplayName(Month.MARCH),
                "comment3", "3", "30", "300"),
            tuple(consentYearString,
                getMonthDisplayName(Month.APRIL),
                "comment4", "4", "40", "400"),
            tuple(consentYearString,
                getMonthDisplayName(Month.MAY),
                "comment5", "5", "50", "500"),
            tuple(consentYearString,
                getMonthDisplayName(Month.JUNE),
                "comment6", "6", "60", "600"),
            tuple(consentYearString,
                getMonthDisplayName(Month.JULY),
                "comment7", "7", "70", "700"),
            tuple(consentYearString,
                getMonthDisplayName(Month.AUGUST),
                "comment8", "8", "80", "800"),
            tuple(consentYearString,
                getMonthDisplayName(Month.SEPTEMBER),
                "comment9", "9", "90", "900"),
            tuple(consentYearString,
                getMonthDisplayName(Month.OCTOBER),
                "comment10", "10", "100", "1000"),
            tuple(consentYearString,
                getMonthDisplayName(Month.NOVEMBER),
                "comment11", "11", "110", "1100"),
            tuple(consentYearString,
                getMonthDisplayName(Month.DECEMBER),
                "comment12", "12", "120", "1200"));
  }

  @Test
  void saveFlareAnnual() {
    flareAnnualService.saveFlareAnnual(applicationVersion, FlareAnnualTestUtil.getFullFlareAnnualFormForYear(2022));

    verify(flareAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);

    ArgumentCaptor<FlareAnnualMonth> flareAnnualMonthArgumentCaptor = ArgumentCaptor.forClass(FlareAnnualMonth.class);
    verify(flareAnnualMonthRepository, times(12)).save(flareAnnualMonthArgumentCaptor.capture());
  }

  private String getMonthDisplayName(Month month) {
    return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
  }

  private void assertOnlyStubData(List<FlareAnnualMonthForm> flareAnnualMonthForms, String consentYearString) {

    assertThat(flareAnnualMonthForms)
        .extracting(FlareAnnualMonthForm::getYear,
            FlareAnnualMonthForm::getMonth,
            form -> form.getComments().getInputValue(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getInputValue())
        .containsExactly(
            tuple(consentYearString,
                getMonthDisplayName(Month.JANUARY),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.FEBRUARY),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.MARCH),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.APRIL),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.MAY),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.JUNE),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.JULY),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.AUGUST),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.SEPTEMBER),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.OCTOBER),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.NOVEMBER),
                null, null, null, null),
            tuple(consentYearString,
                getMonthDisplayName(Month.DECEMBER),
                null, null, null, null));

  }

}

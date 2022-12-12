package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

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
class VentAnnualServiceTest {

  @Mock
  private VentAnnualMonthRepository ventAnnualMonthRepository;

  @Mock
  private ConsentLengthService consentLengthService;

  private VentAnnualService ventAnnualService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    ventAnnualService = new VentAnnualService(ventAnnualMonthRepository, consentLengthService);
    applicationVersion = VentAnnualTestUtil.ventAppVersion;
  }

  @Test
  void ventAnnualMonthsExist() {
    when(ventAnnualMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(true);

    assertThat(ventAnnualService.ventAnnualMonthsExist(applicationVersion)).isTrue();
  }

  @Test
  void ventAnnualMonthsExist_false() {
    when(ventAnnualMonthRepository.existsByApplicationVersion(applicationVersion)).thenReturn(false);

    assertThat(ventAnnualService.ventAnnualMonthsExist(applicationVersion)).isFalse();
  }


  @Test
  void ventAnnualMonthsComplete_noAnnualMonths() {
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion));
    when(ventAnnualMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    assertThat(ventAnnualService.ventAnnualMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void ventAnnualMonthsComplete_annualMonthsMissing() {
    List<VentAnnualMonth> ventAnnualMonths =
        VentAnnualTestUtil.getVentAnnualMonthsForYear(applicationVersion, ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);
    ventAnnualMonths.remove(0); // remove first month

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion));
    when(ventAnnualMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(ventAnnualMonths);

    assertThat(ventAnnualService.ventAnnualMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void ventAnnualComplete_annualMonthsAlign() {
    List<VentAnnualMonth> ventAnnualMonths =
        VentAnnualTestUtil.getVentAnnualMonthsForYear(applicationVersion, ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);

    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion));
    when(ventAnnualMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(ventAnnualMonths);

    assertThat(ventAnnualService.ventAnnualMonthsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getVentAnnualForm_noExistingData() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentYear = consentLengthDetails.getAnnualConsentYear();
    String consentYearString = String.valueOf(consentYear);

    when(ventAnnualMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    VentAnnualForm ventAnnualForm = ventAnnualService.getVentAnnualForm(applicationVersion);

    assertThat(ventAnnualForm.getYear()).isEqualTo(consentYearString);

    assertOnlyStubData(ventAnnualForm.getVentAnnualMonthForms(), consentYearString);
  }

  @Test
  void getVentAnnualForm_existingDataNotOverlapping() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentYear = consentLengthDetails.getAnnualConsentYear();
    String consentYearString = String.valueOf(consentYear);

    List<VentAnnualMonth> ventAnnualMonths =
        VentAnnualTestUtil.getVentAnnualMonthsForYear(applicationVersion, consentYear - 1);

    when(ventAnnualMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventAnnualMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    VentAnnualForm ventAnnualForm = ventAnnualService.getVentAnnualForm(applicationVersion);

    assertThat(ventAnnualForm.getYear()).isEqualTo(consentYearString);

    assertOnlyStubData(ventAnnualForm.getVentAnnualMonthForms(), consentYearString);
  }


  @Test
  void getVentAnnualForm_existingDataOverlapping() {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentYear = consentLengthDetails.getAnnualConsentYear();
    String consentYearString = String.valueOf(consentYear);

    List<VentAnnualMonth> ventAnnualMonths =
        VentAnnualTestUtil.getVentAnnualMonthsForYear(applicationVersion, consentYear);

    when(ventAnnualMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventAnnualMonths);
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);

    VentAnnualForm ventAnnualForm = ventAnnualService.getVentAnnualForm(applicationVersion);

    assertThat(ventAnnualForm.getYear()).isEqualTo(consentYearString);

    assertThat(ventAnnualForm.getVentAnnualMonthForms())
        .extracting(VentAnnualMonthForm::getYear,
            VentAnnualMonthForm::getMonth,
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
  void saveVentAnnual() {
    ventAnnualService.saveVentAnnual(applicationVersion, VentAnnualTestUtil.getFullVentAnnualFormForYear(2022));

    verify(ventAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);

    ArgumentCaptor<VentAnnualMonth> ventAnnualMonthArgumentCaptor = ArgumentCaptor.forClass(VentAnnualMonth.class);
    verify(ventAnnualMonthRepository, times(12)).save(ventAnnualMonthArgumentCaptor.capture());
  }

  private String getMonthDisplayName(Month month) {
    return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
  }

  private void assertOnlyStubData(List<VentAnnualMonthForm> ventAnnualMonthForms, String consentYearString) {

    assertThat(ventAnnualMonthForms)
        .extracting(VentAnnualMonthForm::getYear,
            VentAnnualMonthForm::getMonth,
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

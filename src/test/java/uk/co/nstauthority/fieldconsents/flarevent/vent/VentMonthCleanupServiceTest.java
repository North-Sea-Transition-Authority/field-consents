package uk.co.nstauthority.fieldconsents.flarevent.vent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentTestUtil;

@ExtendWith(MockitoExtension.class)
class VentMonthCleanupServiceTest {

  @Mock
  private VentAnnualMonthRepository ventAnnualMonthRepository;

  @Mock
  private VentAnnualService ventAnnualService;

  @Mock
  private VentShortTermMonthRepository ventShortTermMonthRepository;

  @Mock
  private VentShortTermService ventShortTermService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentLengthService consentLengthService;

  private VentMonthCleanupService ventMonthCleanupService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthChangeEvent consentLengthChangeEvent;

  private List<VentShortTermMonth> existingVentShortTermMonths;

  private List<VentAnnualMonth> existingVentAnnualMonths;

  @BeforeEach
  void setup() {
    ventMonthCleanupService = new VentMonthCleanupService(
        ventAnnualMonthRepository,
        ventAnnualService,
        ventShortTermMonthRepository,
        ventShortTermService,
        applicationVersionService,
        consentLengthService);
    applicationVersion = VentTestUtil.ventAppVersion;
    consentLengthChangeEvent = new ConsentLengthChangeEvent(consentLengthService, applicationVersion.getId());
    when(applicationVersionService.getApplicationVersionById(applicationVersion.getId()))
        .thenReturn(applicationVersion);

    existingVentShortTermMonths =
        VentShortTermTestUtil.getVentShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE, ConsentLengthTestUtil.SHORT_TERM_END_DATE);
    existingVentAnnualMonths =
        VentAnnualTestUtil.getVentAnnualMonthsForYear(applicationVersion, ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);
  }

  @Test
  void onApplicationEvent_flareApplication() {
    ApplicationVersion flareAppVersion = FlareTestUtil.flareAppVersion;
    when(applicationVersionService.getApplicationVersionById(flareAppVersion.getId()))
        .thenReturn(flareAppVersion);
    ventMonthCleanupService.onApplicationEvent(
        new ConsentLengthChangeEvent(consentLengthService, FlareTestUtil.flareAppVersion.getId()));
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_ventApplication_badConsentLengthType() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThatThrownBy(() -> ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Incorrect consent length type: " + ConsentLengthType.LONG_TERM);
  }

  @Test
  void onApplicationEvent_whenShortTerm_noPreviousData() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventShortTermService.getVentShortTermMonths(applicationVersion)).thenReturn(new ArrayList<>());

    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventShortTermService, times(1)).getVentShortTermMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewStartDate() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermStartDate(LocalDate.of(2022, 12, 2));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventShortTermService.getVentShortTermMonths(applicationVersion)).thenReturn(existingVentShortTermMonths);

    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventShortTermService, times(1)).getVentShortTermMonths(applicationVersion);
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(0)); // Oct
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(1)); // Nov
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(2)); // Dec
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewEndDate() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermEndDate(LocalDate.of(2023, 3, 5));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventShortTermService.getVentShortTermMonths(applicationVersion)).thenReturn(existingVentShortTermMonths);

    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventShortTermService, times(1)).getVentShortTermMonths(applicationVersion);
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(5)); // Mar
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(6)); // Apr
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewStartAndEndDates() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermStartDate(LocalDate.of(2022, 11, 2));
    consentLengthDetails.setShortTermEndDate(LocalDate.of(2023, 3, 29));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventShortTermService.getVentShortTermMonths(applicationVersion)).thenReturn(existingVentShortTermMonths);

    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventShortTermService, times(1)).getVentShortTermMonths(applicationVersion);
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(0)); // Oct
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(1)); // Nov
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(5)); // Mar
    verify(ventShortTermMonthRepository, times(1)).delete(existingVentShortTermMonths.get(6)); // Apr
    verifyNoMoreInteractionsAll();

    // mimic the deletes from above on the existing vent month rows
    existingVentShortTermMonths.remove(6); // Apr 2023
    existingVentShortTermMonths.remove(5); // Mar 2023
    existingVentShortTermMonths.remove(1); // Nov 2022
    existingVentShortTermMonths.remove(0); // Oct 2022

    when(ventShortTermService.getVentShortTermMonths(applicationVersion)).thenReturn(existingVentShortTermMonths);

    // verify no more short term data deleted on another change event call
    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventAnnualMonthRepository, times(2)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventShortTermService, times(2)).getVentShortTermMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenAnnual_noPreviousData() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventAnnualService.getVentAnnualMonths(applicationVersion)).thenReturn(new ArrayList<>());

    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventShortTermMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventAnnualService, times(1)).getVentAnnualMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenAnnual_withPreviousDataSameConsentYear() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventAnnualService.getVentAnnualMonths(applicationVersion)).thenReturn(existingVentAnnualMonths);

    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventShortTermMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventAnnualService, times(1)).getVentAnnualMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenAnnual_withPreviousDataAndNewConsentYear() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    consentLengthDetails.setAnnualConsentYear(2024);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventAnnualService.getVentAnnualMonths(applicationVersion)).thenReturn(existingVentAnnualMonths);

    ventMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(ventShortTermMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(ventAnnualService, times(1)).getVentAnnualMonths(applicationVersion);
    for (VentAnnualMonth ventAnnualMonth: existingVentAnnualMonths) {
      verify(ventAnnualMonthRepository, times(1)).delete(ventAnnualMonth);
    }
    verifyNoMoreInteractionsAll();
  }

  private void verifyNoMoreInteractionsAll() {
    verifyNoMoreInteractions(applicationVersionService);
    verifyNoMoreInteractions(consentLengthService);
    verifyNoMoreInteractions(ventAnnualMonthRepository);
    verifyNoMoreInteractions(ventAnnualService);
    verifyNoMoreInteractions(ventShortTermMonthRepository);
    verifyNoMoreInteractions(ventShortTermService);
  }

}

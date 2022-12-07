package uk.co.nstauthority.fieldconsents.flarevent.flare;

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
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualMonth;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.VentTestUtil;

@ExtendWith(MockitoExtension.class)
class FlareMonthCleanupServiceTest {

  @Mock
  private FlareAnnualMonthRepository flareAnnualMonthRepository;

  @Mock
  private FlareAnnualService flareAnnualService;

  @Mock
  private FlareShortTermMonthRepository flareShortTermMonthRepository;

  @Mock
  private FlareShortTermService flareShortTermService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentLengthService consentLengthService;

  private FlareMonthCleanupService flareMonthCleanupService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthChangeEvent consentLengthChangeEvent;

  private List<FlareShortTermMonth> existingFlareShortTermMonths;

  private List<FlareAnnualMonth> existingFlareAnnualMonths;

  @BeforeEach
  void setup() {
    flareMonthCleanupService = new FlareMonthCleanupService(
        flareAnnualMonthRepository,
        flareAnnualService,
        flareShortTermMonthRepository,
        flareShortTermService,
        applicationVersionService,
        consentLengthService);
    applicationVersion = FlareTestUtil.flareAppVersion;
    consentLengthChangeEvent = new ConsentLengthChangeEvent(consentLengthService, applicationVersion.getId());
    when(applicationVersionService.getApplicationVersionById(applicationVersion.getId()))
        .thenReturn(applicationVersion);

    existingFlareShortTermMonths =
        FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(applicationVersion,
            ConsentLengthTestUtil.SHORT_TERM_START_DATE, ConsentLengthTestUtil.SHORT_TERM_END_DATE);
    existingFlareAnnualMonths =
        FlareAnnualTestUtil.getFlareAnnualMonthsForYear(applicationVersion, ConsentLengthTestUtil.ANNUAL_CONSENT_YEAR);
  }

  @Test
  void onApplicationEvent_ventApplication() {
    ApplicationVersion ventAppVersion = VentTestUtil.ventAppVersion;
    when(applicationVersionService.getApplicationVersionById(ventAppVersion.getId()))
        .thenReturn(ventAppVersion);
    flareMonthCleanupService.onApplicationEvent(
        new ConsentLengthChangeEvent(consentLengthService, VentTestUtil.ventAppVersion.getId()));
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_flareApplication_badConsentLengthType() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThatThrownBy(() -> flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Incorrect consent length type: " + ConsentLengthType.LONG_TERM);
  }

  @Test
  void onApplicationEvent_whenShortTerm_noPreviousData() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareShortTermService.getFlareShortTermMonths(applicationVersion)).thenReturn(new ArrayList<>());

    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareShortTermService, times(1)).getFlareShortTermMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewStartDate() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermStartDate(LocalDate.of(2022, 12, 2));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareShortTermService.getFlareShortTermMonths(applicationVersion)).thenReturn(existingFlareShortTermMonths);

    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareShortTermService, times(1)).getFlareShortTermMonths(applicationVersion);
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(0)); // Oct
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(1)); // Nov
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(2)); // Dec
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewEndDate() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermEndDate(LocalDate.of(2023, 3, 5));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareShortTermService.getFlareShortTermMonths(applicationVersion)).thenReturn(existingFlareShortTermMonths);

    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareShortTermService, times(1)).getFlareShortTermMonths(applicationVersion);
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(5)); // Mar
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(6)); // Apr
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewStartAndEndDates() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermStartDate(LocalDate.of(2022, 11, 2));
    consentLengthDetails.setShortTermEndDate(LocalDate.of(2023, 3, 29));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareShortTermService.getFlareShortTermMonths(applicationVersion)).thenReturn(existingFlareShortTermMonths);

    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareAnnualMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareShortTermService, times(1)).getFlareShortTermMonths(applicationVersion);
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(0)); // Oct
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(1)); // Nov
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(5)); // Mar
    verify(flareShortTermMonthRepository, times(1)).delete(existingFlareShortTermMonths.get(6)); // Apr
    verifyNoMoreInteractionsAll();

    // mimic the deletes from above on the existing flare month rows
    existingFlareShortTermMonths.remove(6); // Apr 2023
    existingFlareShortTermMonths.remove(5); // Mar 2023
    existingFlareShortTermMonths.remove(1); // Nov 2022
    existingFlareShortTermMonths.remove(0); // Oct 2022

    when(flareShortTermService.getFlareShortTermMonths(applicationVersion)).thenReturn(existingFlareShortTermMonths);

    // verify no more short term data deleted on another change event call
    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareAnnualMonthRepository, times(2)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareShortTermService, times(2)).getFlareShortTermMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenAnnual_noPreviousData() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareAnnualService.getFlareAnnualMonths(applicationVersion)).thenReturn(new ArrayList<>());

    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareShortTermMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareAnnualService, times(1)).getFlareAnnualMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenAnnual_withPreviousDataSameConsentYear() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareAnnualService.getFlareAnnualMonths(applicationVersion)).thenReturn(existingFlareAnnualMonths);

    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareShortTermMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareAnnualService, times(1)).getFlareAnnualMonths(applicationVersion);
    verifyNoMoreInteractionsAll();
  }

  @Test
  void onApplicationEvent_whenAnnual_withPreviousDataAndNewConsentYear() {
    ConsentLengthDetails consentLengthDetails =
        ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    consentLengthDetails.setAnnualConsentYear(2024);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareAnnualService.getFlareAnnualMonths(applicationVersion)).thenReturn(existingFlareAnnualMonths);

    flareMonthCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(flareShortTermMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(flareAnnualService, times(1)).getFlareAnnualMonths(applicationVersion);
    for (FlareAnnualMonth flareAnnualMonth: existingFlareAnnualMonths) {
      verify(flareAnnualMonthRepository, times(1)).delete(flareAnnualMonth);
    }
    verifyNoMoreInteractionsAll();
  }

  private void verifyNoMoreInteractionsAll() {
    verifyNoMoreInteractions(applicationVersionService);
    verifyNoMoreInteractions(consentLengthService);
    verifyNoMoreInteractions(flareAnnualMonthRepository);
    verifyNoMoreInteractions(flareAnnualService);
    verifyNoMoreInteractions(flareShortTermMonthRepository);
    verifyNoMoreInteractions(flareShortTermService);
  }

}

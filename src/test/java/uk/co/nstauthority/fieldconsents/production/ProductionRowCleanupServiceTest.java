package uk.co.nstauthority.fieldconsents.production;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonthRepository;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYearRepository;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonthRepository;

@ExtendWith(MockitoExtension.class)
class ProductionRowCleanupServiceTest {

  @Mock
  private ShortTermProductionMonthRepository shortTermProductionMonthRepository;

  @Mock
  private AnnualProductionMonthRepository annualProductionMonthRepository;

  @Mock
  private LongTermProductionYearRepository longTermProductionYearRepository;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentLengthService consentLengthService;

  private ProductionRowCleanupService productionRowCleanupService;

  private ApplicationVersion applicationVersion;

  private ConsentLengthChangeEvent consentLengthChangeEvent;


  @BeforeEach
  void setup() {
    productionRowCleanupService = new ProductionRowCleanupService(
        shortTermProductionMonthRepository,
        annualProductionMonthRepository,
        longTermProductionYearRepository,
        applicationVersionService,
        consentLengthService
    );
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    consentLengthChangeEvent = new ConsentLengthChangeEvent(
        consentLengthService,
        applicationVersion.getId()
    );
    when(applicationVersionService.getApplicationVersionById(applicationVersion.getId())).thenReturn(applicationVersion);
  }

  @Test
  void onApplicationEvent_whenShortTerm_noPreviousData() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(shortTermProductionMonthRepository.findAllByApplicationVersionOrderByStartDate(applicationVersion)).thenReturn(
        Collections.emptyList()
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(annualProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(shortTermProductionMonthRepository, times(1)).findAllByApplicationVersionOrderByStartDate(applicationVersion);
    verifyNoMoreInteractions(shortTermProductionMonthRepository);
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewStartDate() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermStartDate(LocalDate.of(2022, 12, 20));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(shortTermProductionMonthRepository.findAllByApplicationVersionOrderByStartDate(applicationVersion)).thenReturn(
        ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion)
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(annualProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(shortTermProductionMonthRepository, times(1)).findAllByApplicationVersionOrderByStartDate(applicationVersion);
    verify(shortTermProductionMonthRepository, times(4)).delete(any(ShortTermProductionMonth.class));
    verifyNoMoreInteractions(shortTermProductionMonthRepository);
  }

  @Test
  void onApplicationEvent_whenShortTerm_withPreviousDataAndNewEndDate() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
    consentLengthDetails.setShortTermEndDate(LocalDate.of(2023, 3, 5));

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(shortTermProductionMonthRepository.findAllByApplicationVersionOrderByStartDate(applicationVersion)).thenReturn(
        ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion)
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(annualProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(shortTermProductionMonthRepository, times(1)).findAllByApplicationVersionOrderByStartDate(applicationVersion);
    verify(shortTermProductionMonthRepository, times(3)).delete(any(ShortTermProductionMonth.class));
    verifyNoMoreInteractions(shortTermProductionMonthRepository);
  }

  @Test
  void onApplicationEvent_whenAnnual_noPreviousData() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(annualProductionMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(
        Collections.emptyList()
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(shortTermProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(annualProductionMonthRepository, times(1)).findAllByApplicationVersion(applicationVersion);
    verifyNoMoreInteractions(annualProductionMonthRepository);
  }

  @Test
  void onApplicationEvent_whenAnnual_withPreviousDataAndNewConsentYear() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    consentLengthDetails.setAnnualConsentYear(2023);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(annualProductionMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(
        ProductionTestUtils.getAnnualProductionMonthsData(applicationVersion)
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(shortTermProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(annualProductionMonthRepository, times(12)).delete(any(AnnualProductionMonth.class));
    verifyNoMoreInteractions(annualProductionMonthRepository);
  }

  @Test
  void onApplicationEvent_whenLongTerm_noPreviousData() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion)).thenReturn(
        Collections.emptyList()
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(annualProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(shortTermProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(1)).findAllByApplicationVersionOrderByYearAsc(applicationVersion);
    verifyNoMoreInteractions(longTermProductionYearRepository);
  }

  @Test
  void onApplicationEvent_whenLongTerm_withPreviousDataAndNewStartYear() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    consentLengthDetails.setLongTermStartYear(2024);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion)).thenReturn(
        ProductionTestUtils.getLongTermProductionYearsData(applicationVersion)
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(annualProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(shortTermProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(2)).delete(any(LongTermProductionYear.class));
    verifyNoMoreInteractions(longTermProductionYearRepository);
  }

  @Test
  void onApplicationEvent_whenLongTerm_withPreviousDataAndNewEndYear() {
    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);
    consentLengthDetails.setLongTermEndYear(2024);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion)).thenReturn(
        ProductionTestUtils.getLongTermProductionYearsData(applicationVersion)
    );

    productionRowCleanupService.onApplicationEvent(consentLengthChangeEvent);

    verify(annualProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(shortTermProductionMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);
    verify(longTermProductionYearRepository, times(2)).delete(any(LongTermProductionYear.class));
    verifyNoMoreInteractions(longTermProductionYearRepository);
  }
}
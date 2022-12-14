package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class FlareReportCleanupServiceTest {

  @Mock
  private FlareReportMonthRepository flareReportMonthRepository;

  private FlareReportCleanupService flareReportCleanupService;

  private ApplicationVersion applicationVersion;

  private FlareReportPeriod flareReportPeriod;

  @BeforeEach
  void setup() {
    flareReportCleanupService = new FlareReportCleanupService(flareReportMonthRepository);
    applicationVersion = FlareReportTestUtil.flareAppVersion;
    flareReportPeriod = new FlareReportPeriod(applicationVersion, Month.DECEMBER, 2022);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_noExistingData() {
    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());

    flareReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, flareReportPeriod);

    verifyNoMoreInteractions(flareReportMonthRepository);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_existingDataNoneToDelete() {
    List<FlareReportMonth> flareReportMonths =
        FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, 2022);

    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareReportMonths);

    flareReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, flareReportPeriod);

    verifyNoMoreInteractions(flareReportMonthRepository);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_existingDataWrongYear() {
    List<FlareReportMonth> flareReportMonths =
        FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, 2021);

    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareReportMonths);

    flareReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, flareReportPeriod);

    // verify all report data is deleted
    for (FlareReportMonth flareReportMonth: flareReportMonths) {
      verify(flareReportMonthRepository, times(1)).delete(flareReportMonth);
    }

    verifyNoMoreInteractions(flareReportMonthRepository);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_existingDataOverlapping() {
    List<FlareReportMonth> flareReportMonths =
        FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, 2022);

    flareReportPeriod = new FlareReportPeriod(applicationVersion, Month.JUNE, 2023);

    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareReportMonths);

    flareReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, flareReportPeriod);

    // verify Jan to June 2022 report data is deleted
    verify(flareReportMonthRepository, times(1)).delete(flareReportMonths.get(0));
    verify(flareReportMonthRepository, times(1)).delete(flareReportMonths.get(1));
    verify(flareReportMonthRepository, times(1)).delete(flareReportMonths.get(2));
    verify(flareReportMonthRepository, times(1)).delete(flareReportMonths.get(3));
    verify(flareReportMonthRepository, times(1)).delete(flareReportMonths.get(4));
    verify(flareReportMonthRepository, times(1)).delete(flareReportMonths.get(5));

    verifyNoMoreInteractions(flareReportMonthRepository);
  }

}

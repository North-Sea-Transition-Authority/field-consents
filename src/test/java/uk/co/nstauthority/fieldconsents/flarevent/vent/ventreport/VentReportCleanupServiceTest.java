package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class VentReportCleanupServiceTest {

  @Mock
  private VentReportMonthRepository ventReportMonthRepository;

  private VentReportCleanupService ventReportCleanupService;

  private ApplicationVersion applicationVersion;

  private VentReportPeriod ventReportPeriod;

  @BeforeEach
  void setup() {
    ventReportCleanupService = new VentReportCleanupService(ventReportMonthRepository);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    ventReportPeriod = new VentReportPeriod(applicationVersion, Month.DECEMBER, 2022);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_noExistingData() {
    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());

    ventReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, ventReportPeriod);

    verifyNoMoreInteractions(ventReportMonthRepository);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_existingDataNoneToDelete() {
    List<VentReportMonth> ventReportMonths =
        VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, 2022);

    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventReportMonths);

    ventReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, ventReportPeriod);

    verifyNoMoreInteractions(ventReportMonthRepository);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_existingDataWrongYear() {
    List<VentReportMonth> ventReportMonths =
        VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, 2021);

    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventReportMonths);

    ventReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, ventReportPeriod);

    // verify all report data is deleted
    for (VentReportMonth ventReportMonth: ventReportMonths) {
      verify(ventReportMonthRepository, times(1)).delete(ventReportMonth);
    }

    verifyNoMoreInteractions(ventReportMonthRepository);
  }

  @Test
  void removeObsoleteReportDataOnPeriodSave_existingDataOverlapping() {
    List<VentReportMonth> ventReportMonths =
        VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, 2022);

    ventReportPeriod = new VentReportPeriod(applicationVersion, Month.JUNE, 2023);

    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventReportMonths);

    ventReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, ventReportPeriod);

    // verify Jan to June 2022 report data is deleted
    verify(ventReportMonthRepository, times(1)).delete(ventReportMonths.get(0));
    verify(ventReportMonthRepository, times(1)).delete(ventReportMonths.get(1));
    verify(ventReportMonthRepository, times(1)).delete(ventReportMonths.get(2));
    verify(ventReportMonthRepository, times(1)).delete(ventReportMonths.get(3));
    verify(ventReportMonthRepository, times(1)).delete(ventReportMonths.get(4));
    verify(ventReportMonthRepository, times(1)).delete(ventReportMonths.get(5));

    verifyNoMoreInteractions(ventReportMonthRepository);
  }

}

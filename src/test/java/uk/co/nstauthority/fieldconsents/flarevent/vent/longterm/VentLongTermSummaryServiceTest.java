package uk.co.nstauthority.fieldconsents.flarevent.vent.longterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.vent.longterm.VentLongTermTestUtil.getVentLongTermYears;

import java.util.Collections;
import java.util.Comparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionConsentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTestUtil;

@ExtendWith(MockitoExtension.class)
class VentLongTermSummaryServiceTest {

  @Mock
  private VentLongTermYearRepository ventLongTermYearRepository;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private EmissionConsentSummaryService emissionConsentSummaryService;

  @InjectMocks
  private VentLongTermSummaryService ventLongTermSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void getVentLongTermSummaryCard_noLongTermYears() {
    when(ventLongTermYearRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(ventLongTermSummaryService.getVentLongTermSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());

    verifyNoInteractions(applicationUnitService);
    verifyNoInteractions(emissionConsentSummaryService);
  }

  @Test
  void getVentLongTermSummaryCard_longTermYearsExist() {
    var categoryUnit = FlareVentUnit.TONNES_PER_DAY;
    var tableSummaryCard = SummaryTestUtil.getTableSummaryCard();
    var ventLongTermYears = getVentLongTermYears(applicationVersion, 2021, 2025);
    var ventLongTermYearsOrdered = ventLongTermYears
        .stream()
        .sorted(Comparator.comparing(EmissionLongTermYear::getYear))
        .toList();

    when(ventLongTermYearRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventLongTermYears);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion))
        .thenReturn(categoryUnit);
    when(emissionConsentSummaryService.getLongTermConsentSummaryCard(ventLongTermYearsOrdered, categoryUnit))
        .thenReturn(tableSummaryCard);

    assertThat(ventLongTermSummaryService.getVentLongTermSummaryCard(applicationVersion))
        .isEqualTo(tableSummaryCard);

    verify(applicationUnitService, times(1))
        .getVentCategoryUnit(applicationVersion);
    verify(emissionConsentSummaryService, times(1))
        .getLongTermConsentSummaryCard(ventLongTermYearsOrdered, categoryUnit);
  }
}

package uk.co.nstauthority.fieldconsents.flarevent.flare.longterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.flare.longterm.FlareLongTermTestUtil.getFlareLongTermYears;

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
class FlareLongTermSummaryServiceTest {

  @Mock
  private FlareLongTermYearRepository flareLongTermYearRepository;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private EmissionConsentSummaryService emissionConsentSummaryService;

  @InjectMocks
  private FlareLongTermSummaryService flareLongTermSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getFlareLongTermSummaryCard_noLongTermYears() {
    when(flareLongTermYearRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(flareLongTermSummaryService.getFlareLongTermSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());

    verifyNoInteractions(applicationUnitService);
    verifyNoInteractions(emissionConsentSummaryService);
  }

  @Test
  void getFlareLongTermSummaryCard_longTermYearsExist() {
    var categoryUnit = FlareVentUnit.TONNES_PER_DAY;
    var tableSummaryCard = SummaryTestUtil.getTableSummaryCard();
    var flareLongTermYears = getFlareLongTermYears(applicationVersion, 2021, 2025);
    var flareLongTermYearsOrdered = flareLongTermYears
        .stream()
        .sorted(Comparator.comparing(EmissionLongTermYear::getYear))
        .toList();

    when(flareLongTermYearRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareLongTermYears);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion))
        .thenReturn(categoryUnit);
    when(emissionConsentSummaryService.getLongTermConsentSummaryCard(flareLongTermYearsOrdered, categoryUnit))
        .thenReturn(tableSummaryCard);

    assertThat(flareLongTermSummaryService.getFlareLongTermSummaryCard(applicationVersion))
        .isEqualTo(tableSummaryCard);

    verify(applicationUnitService, times(1))
        .getFlareCategoryUnit(applicationVersion);
    verify(emissionConsentSummaryService, times(1))
        .getLongTermConsentSummaryCard(flareLongTermYearsOrdered, categoryUnit);
  }
}

package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_1_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_2_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_3_HEADING;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.LONG_MONTH_YEAR;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport.FlareReport123Service;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;

@ExtendWith(MockitoExtension.class)
class FlareReport123GasDataSummaryServiceTest {

  @Mock
  private FlareReport123GasDataRepository flareReport123GasDataRepository;

  @Mock
  private FlareReport123Service flareReport123Service;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private FlareReport123GasDataSummaryService flareReport123GasDataSummaryService;

  private ApplicationVersion applicationVersion;

  private FlareReport123GasData flareReport123GasData;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    flareReport123GasData = FlareReport123GasDataTestUtil.getCompleteAndValidFlareReport123GasData(applicationVersion);
  }

  @Test
  void getFlareReport123GasDataSummaryCard_noGasData() {
    when(flareReport123GasDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(flareReport123GasDataSummaryService.getFlareReport123GasDataSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getFlareReport123GasDataSummaryCard() {
    var densityUnit = FlareVentUnit.G_PER_MOL;
    var gasContentUnit = FlareVentUnit.MOL_PERCENTAGE;
    var reportStart = YearMonth.of(2022, Month.OCTOBER);
    var reportEnd = YearMonth.of(2023, Month.SEPTEMBER);

    when(flareReport123GasDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(flareReport123GasData));
    when(applicationUnitService.getFlareGasDensityUnit(applicationVersion))
        .thenReturn(densityUnit);
    when(applicationUnitService.getFlareGasContentUnit(applicationVersion))
        .thenReturn(gasContentUnit);
    when(flareReport123Service.getStartYearMonth(applicationVersion))
        .thenReturn(reportStart);
    when(flareReport123Service.getEndYearMonth(applicationVersion))
        .thenReturn(reportEnd);

    assertThat(flareReport123GasDataSummaryService.getFlareReport123GasDataSummaryCard(applicationVersion))
        .isEqualTo(
            new SummaryCard(
                DateUtils.format(reportStart, LONG_MONTH_YEAR) + " to " +
                    DateUtils.format(reportEnd, LONG_MONTH_YEAR),
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        new SummaryTableRow(Stream.of(
                            null,
                            CATEGORY_1_HEADING,
                            CATEGORY_2_HEADING,
                            CATEGORY_3_HEADING
                        ).toList()),
                        new SummaryTableRow(List.of(
                            "Stream mol wt (%s)".formatted(densityUnit.getDisplayName()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory1Density()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory2Density()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory3Density())
                        )),
                        new SummaryTableRow(List.of(
                            "Inert gas content (%s)".formatted(gasContentUnit.getDisplayName()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory1InertGasPercentage()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory2InertGasPercentage()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory3InertGasPercentage())
                        )),
                        new SummaryTableRow(List.of(
                            "Hydrocarbon content (%s)".formatted(gasContentUnit.getDisplayName()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory1HydrocarbonPercentage()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory2HydrocarbonPercentage()),
                            bigDecimalToFormattedString(flareReport123GasData.getCategory3HydrocarbonPercentage())
                        ))
                    )
                )
            )
        );
  }
}

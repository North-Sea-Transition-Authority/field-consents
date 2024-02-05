package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.CATEGORY_1_HEADING;
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
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport.VentReport123Service;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;

@ExtendWith(MockitoExtension.class)
class VentReport123GasDataSummaryServiceTest {

  @Mock
  private VentReport123GasDataRepository ventReport123GasDataRepository;

  @Mock
  private VentReport123Service ventReport123Service;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private VentReport123GasDataSummaryService ventReport123GasDataSummaryService;

  private ApplicationVersion applicationVersion;

  private VentReport123GasData ventReport123GasData;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    ventReport123GasData = VentReport123GasDataTestUtil.getCompleteAndValidVentReport123GasData(applicationVersion);
  }

  @Test
  void getVentReport123GasDataSummaryCard_noGasData() {
    when(ventReport123GasDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(ventReport123GasDataSummaryService.getVentReport123GasDataSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getVentReport123GasDataSummaryCard() {
    var densityUnit = FlareVentUnit.G_PER_MOL;
    var gasContentUnit = FlareVentUnit.MOL_PERCENTAGE;
    var reportStart = YearMonth.of(2022, Month.OCTOBER);
    var reportEnd = YearMonth.of(2023, Month.SEPTEMBER);

    when(ventReport123GasDataRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(ventReport123GasData));
    when(applicationUnitService.getVentGasDensityUnit(applicationVersion))
        .thenReturn(densityUnit);
    when(applicationUnitService.getVentGasContentUnit(applicationVersion))
        .thenReturn(gasContentUnit);
    when(ventReport123Service.getStartYearMonth(applicationVersion))
        .thenReturn(reportStart);
    when(ventReport123Service.getEndYearMonth(applicationVersion))
        .thenReturn(reportEnd);

    assertThat(ventReport123GasDataSummaryService.getVentReport123GasDataSummaryCard(applicationVersion))
        .isEqualTo(
            new SummaryCard(
                DateUtils.format(reportStart, LONG_MONTH_YEAR) + " to " +
                    DateUtils.format(reportEnd, LONG_MONTH_YEAR),
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        new SummaryTableRow(Stream.of(
                            null,
                            CATEGORY_1_HEADING
                        ).toList()),
                        new SummaryTableRow(List.of(
                            "Stream mol wt (%s)".formatted(densityUnit.getDisplayName()),
                            bigDecimalToFormattedString(ventReport123GasData.getCategory1Density())
                        )),
                        new SummaryTableRow(List.of(
                            "Inert gas content (%s)".formatted(gasContentUnit.getDisplayName()),
                            bigDecimalToFormattedString(ventReport123GasData.getCategory1InertGasPercentage())
                        )),
                        new SummaryTableRow(List.of(
                            "Hydrocarbon content (%s)".formatted(gasContentUnit.getDisplayName()),
                            bigDecimalToFormattedString(ventReport123GasData.getCategory1HydrocarbonPercentage())
                        ))
                    )
                )
            )
        );
  }
}

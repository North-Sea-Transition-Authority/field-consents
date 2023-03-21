package uk.co.nstauthority.fieldconsents.flarevent.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_A_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_B_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_C_HEADING;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.LONG_MONTH_YEAR;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasData;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

@ExtendWith(MockitoExtension.class)
class EmissionReportGasDataSummaryServiceTest {

  @InjectMocks
  private EmissionReportGasDataSummaryService emissionReportGasDataSummaryService;

  private FlareVentReportGasData reportGasData;

  @BeforeEach
  void setUp() {
    reportGasData = FlareVentReportGasTestUtil.getCompleteAndValidFlareReportGasData();
  }

  @Test
  void getReportGasDataTableSummaryCard() {
    var reportPeriod = FlareReportTestUtil.getFullFlareReportPeriod();
    var densityUnit = FlareVentUnit.KG_PER_CUBIC_METER;
    var gasContentUnit = FlareVentUnit.MASS_PERCENTAGE;

    var summaryCard = emissionReportGasDataSummaryService.getReportGasDataTableSummaryCard(
        reportGasData,
        reportPeriod,
        densityUnit,
        gasContentUnit
    );

    assertThat(summaryCard)
        .isEqualTo(
            new SummaryCard(
                DateUtils.format(reportPeriod.getReportStartYearMonth(), LONG_MONTH_YEAR) + " to " +
                    DateUtils.format(reportPeriod.getReportEndYearMonth(), LONG_MONTH_YEAR),
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        new SummaryTableRow(Stream.of(
                            null,
                            CATEGORY_A_HEADING,
                            CATEGORY_B_HEADING,
                            CATEGORY_C_HEADING
                        ).toList()),
                        new SummaryTableRow(List.of(
                            "Standard density (%s)".formatted(densityUnit.getDisplayName()),
                            bigDecimalToFormattedString(reportGasData.getCategoryADensity()),
                            bigDecimalToFormattedString(reportGasData.getCategoryBDensity()),
                            bigDecimalToFormattedString(reportGasData.getCategoryCDensity())
                        )),
                        new SummaryTableRow(List.of(
                            "Inert gas content (%s)".formatted(gasContentUnit.getDisplayName()),
                            bigDecimalToFormattedString(reportGasData.getCategoryAInertGasPercentage()),
                            bigDecimalToFormattedString(reportGasData.getCategoryBInertGasPercentage()),
                            bigDecimalToFormattedString(reportGasData.getCategoryCInertGasPercentage())
                        )),
                        new SummaryTableRow(List.of(
                            "Hydrocarbon content (%s)".formatted(gasContentUnit.getDisplayName()),
                            bigDecimalToFormattedString(reportGasData.getCategoryAHydrocarbonPercentage()),
                            bigDecimalToFormattedString(reportGasData.getCategoryBHydrocarbonPercentage()),
                            bigDecimalToFormattedString(reportGasData.getCategoryCHydrocarbonPercentage())
                        ))
                    )
                )
            )
        );
  }

  @Test
  void getReportGasDataJustificationSummaryCard_evaluatedForEachCategory() {
    assertThat(emissionReportGasDataSummaryService.getReportGasDataJustificationSummaryCard(reportGasData))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.SIMPLE_SUMMARY,
                new SummaryDataView(
                    List.of(
                        new SummaryKeyValue("Have you evaluated the properties for each category individually?",
                            BooleanUtil.yesNoFromBoolean(reportGasData.getEvaluatedPerCategory()))
                    )
                )
            )
        );
  }

  @Test
  void getReportGasDataJustificationSummaryCard_evaluatedForEachCategoryFalse() {
    reportGasData.setEvaluatedPerCategory(Boolean.FALSE);
    reportGasData.setEvaluatedPerCategoryExplanation("test explanation");
    assertThat(emissionReportGasDataSummaryService.getReportGasDataJustificationSummaryCard(reportGasData))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.SIMPLE_SUMMARY,
                new SummaryDataView(
                    List.of(
                        new SummaryKeyValue("Have you evaluated the properties for each category individually?",
                            BooleanUtil.yesNoFromBoolean(reportGasData.getEvaluatedPerCategory())),
                        new SummaryKeyValue("Please provide an explanation why you haven’t evaluated the properties for each category",
                            reportGasData.getEvaluatedPerCategoryExplanation())
                    )
                )
            )
        );
  }
}
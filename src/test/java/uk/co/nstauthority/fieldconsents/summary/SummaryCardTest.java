package uk.co.nstauthority.fieldconsents.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.summary.SummaryTestUtil.summaryDataView;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartData;

class SummaryCardTest {

  private final SummaryTableView summaryTableView = SummaryTestUtil.getSummaryTableView();

  @Test
  void simpleSummaryCardWithHeading() {
    assertThat(SummaryCard.simpleSummaryCardWithHeading("display name", summaryDataView))
        .isEqualTo(
            new SummaryCard(
                "display name",
                SummaryCardType.SIMPLE_SUMMARY,
                summaryDataView
            )
        );
  }

  @Test
  void simpleSummaryCard() {
    assertThat(SummaryCard.simpleSummaryCard(summaryDataView))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.SIMPLE_SUMMARY,
                summaryDataView
            )
        );
  }

  @Test
  void emptySummaryCard() {
    assertThat(SummaryCard.emptySummaryCard())
        .isEqualTo(
          new SummaryCard(
              null,
              SummaryCardType.EMPTY_SUMMARY,
              null
          )
        );
  }

  @Test
  void emptySummaryCardList() {
    assertThat(SummaryCard.emptySummaryCardList())
        .isEqualTo(
            List.of(
                new SummaryCard(
                    null,
                    SummaryCardType.EMPTY_SUMMARY,
                    null
                )
            )
        );
  }

  @Test
  void tableSummaryCardWithHeading() {
    assertThat(SummaryCard.tableSummaryCardWithHeading("display name", summaryTableView))
        .isEqualTo(
            new SummaryCard(
                "display name",
                SummaryCardType.TABLE_SUMMARY,
                summaryTableView
            )
        );
  }

  @Test
  void tableSummaryCard() {
    assertThat(SummaryCard.tableSummaryCard(summaryTableView))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                summaryTableView
            )
        );
  }

  @Test
  void stackedBarChartSummaryCard() throws Exception {
    var objectMapper = new ObjectMapper();
    var emissionsChartData = new EmissionsChartData(
        "Example chart title",
        List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"),
        "Month",
        "Days in month",
        List.of(new EmissionsChartData.Series(
            "Days",
            "highcharts-colour-blue",
            List.of(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        ))
    );
    var emissionsChartDataJson = objectMapper.writeValueAsString(emissionsChartData);

    assertThat(SummaryCard.stackedBarChartSummaryCard(emissionsChartData))
        .isEqualTo(new SummaryCard(
            null,
            SummaryCardType.STACKED_BAR_CHART_SUMMARY,
            emissionsChartDataJson
        ));
  }
}
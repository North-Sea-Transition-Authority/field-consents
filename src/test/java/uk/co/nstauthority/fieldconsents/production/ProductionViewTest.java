package uk.co.nstauthority.fieldconsents.production;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.validation.ValidatorUtils.MAX_DECIMAL_PLACES;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

class ProductionViewTest {

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void fromShortTerm_noProductionMonthsData() {
    var productionView = ProductionView.fromShortTerm(Collections.emptyList(), ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_DAY);
    var emptyProductionView = ProductionViewTestUtil.emptyShortTerm();

    assertThat(productionView)
        .usingRecursiveComparison()
        .isEqualTo(emptyProductionView);
  }

  @Test
  void fromAnnual_noProductionMonthsData() {
    var productionView = ProductionView.fromAnnual(Collections.emptyList(), ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_DAY);
    var emptyProductionView = ProductionViewTestUtil.emptyAnnual();

    assertThat(productionView)
        .usingRecursiveComparison()
        .isEqualTo(emptyProductionView);
  }


  @Test
  void fromLongTerm_noProductionMonthsData() {
    var productionView = ProductionView.fromLongTerm(Collections.emptyList(), ProductionUnit.KSCM_PER_DAY, ProductionUnit.KSCM_PER_DAY);
    var emptyProductionView = ProductionViewTestUtil.emptyLongTerm();

    assertThat(productionView)
        .usingRecursiveComparison()
        .isEqualTo(emptyProductionView);
  }

  @Test
  void fromShortTerm_productionMonthsDataExists() {
    var productionMonths = ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion);
    var productionView = ProductionView.fromShortTerm(productionMonths, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_DAY);
    var emptyProductionView = ProductionViewTestUtil.emptyShortTerm();

    assertHeadings(productionView, emptyProductionView);

    var productionRowViews = productionView.productionRows();

    assertThat(productionRowViews).hasSize(productionMonths.size() + 2);

    for (int i = 0; i < productionMonths.size(); i++) {
      var productionMonth = productionMonths.get(i);
      assertThat(productionRowViews.get(i))
          .extracting(ProductionRowView::rowPrompt,
              ProductionRowView::consentDays,
              ProductionRowView::oilMinValue,
              ProductionRowView::oilMaxValue,
              ProductionRowView::gasMinValue,
              ProductionRowView::gasMaxValue
          )
          .containsExactly(
              productionMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + productionMonth.getYear(),
              String.valueOf(DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate())),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getOilMinValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getOilMaxValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getGasMinValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getGasMaxValue())
          );
    }

    int expectedTotalDays = productionMonths
        .stream()
        .mapToInt(productionMonth -> DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate()))
        .sum();

    assertTotalsAndAverages(productionRowViews, productionMonths, expectedTotalDays);
  }

  @Test
  void fromAnnual_productionMonthsDataExists() {
    var productionMonths = ProductionTestUtils.getAnnualProductionMonthsData(applicationVersion);
    var productionView = ProductionView.fromAnnual(productionMonths, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_DAY);
    var emptyProductionView = ProductionViewTestUtil.emptyAnnual();

    assertHeadings(productionView, emptyProductionView);

    var productionRowViews = productionView.productionRows();

    assertThat(productionRowViews).hasSize(productionMonths.size() + 2);

    for (int i = 0; i < productionMonths.size(); i++) {
      var productionMonth = productionMonths.get(i);
      assertThat(productionRowViews.get(i))
          .extracting(ProductionRowView::rowPrompt,
              ProductionRowView::consentDays,
              ProductionRowView::oilMinValue,
              ProductionRowView::oilMaxValue,
              ProductionRowView::gasMinValue,
              ProductionRowView::gasMaxValue
          )
          .containsExactly(
              productionMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
              String.valueOf(YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getOilMinValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getOilMaxValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getGasMinValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionMonth.getGasMaxValue())
          );
    }

    int expectedTotalDays = productionMonths
        .stream()
        .mapToInt(productionMonth -> YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth())
        .sum();

    assertTotalsAndAverages(productionRowViews, productionMonths, expectedTotalDays);
  }

  @Test
  void fromLongTerm_productionMonthsDataExists() {
    var productionYears = ProductionTestUtils.getLongTermProductionYearsData(applicationVersion);
    var productionView = ProductionView.fromLongTerm(productionYears, ProductionUnit.KSCM_PER_DAY, ProductionUnit.KSCM_PER_DAY);
    var emptyProductionView = ProductionViewTestUtil.emptyLongTerm();

    assertHeadings(productionView, emptyProductionView);

    var productionRowViews = productionView.productionRows();

    assertThat(productionRowViews).hasSize(productionYears.size());

    for (int i = 0; i < productionYears.size(); i++) {
      var productionYear = productionYears.get(i);
      assertThat(productionRowViews.get(i))
          .extracting(ProductionRowView::rowPrompt,
              ProductionRowView::consentDays,
              ProductionRowView::oilMinValue,
              ProductionRowView::oilMaxValue,
              ProductionRowView::gasMinValue,
              ProductionRowView::gasMaxValue
          )
          .containsExactly(
              String.valueOf(productionYear.getYear()),
              String.valueOf(Year.of(productionYear.getYear()).length()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionYear.getOilMinValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionYear.getOilMaxValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionYear.getGasMinValue()),
              DecimalFormatUtils.bigDecimalToFormattedString(productionYear.getGasMaxValue())
          );
    }
  }

  private void assertHeadings(ProductionView productionView, ProductionView expectedProductionView) {
    assertThat(productionView)
        .extracting(
            ProductionView::periodHeading,
            ProductionView::consentDaysHeading,
            ProductionView::oilMinHeading,
            ProductionView::oilMaxHeading,
            ProductionView::gasMinHeading,
            ProductionView::gasMaxHeading)
        .containsExactly(
            expectedProductionView.periodHeading(),
            expectedProductionView.consentDaysHeading(),
            expectedProductionView.oilMinHeading(),
            expectedProductionView.oilMaxHeading(),
            expectedProductionView.gasMinHeading(),
            expectedProductionView.gasMaxHeading()
        );
  }

  private void assertTotalsAndAverages(List<ProductionRowView> productionRowViews,
                                       List<? extends ProductionRow> productionMonths,
                                       int expectedTotalDays) {
    var oilMinTotal = productionMonths.stream().map(ProductionRow::getOilMinValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    var oilMaxTotal = productionMonths.stream().map(ProductionRow::getOilMaxValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    var gasMinTotal = productionMonths.stream().map(ProductionRow::getGasMinValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    var gasMaxTotal = productionMonths.stream().map(ProductionRow::getGasMaxValue).reduce(BigDecimal.ZERO, BigDecimal::add);

    // check totals
    assertThat(productionRowViews.get(productionMonths.size()))
        .extracting(ProductionRowView::rowPrompt,
            ProductionRowView::consentDays,
            ProductionRowView::oilMinValue,
            ProductionRowView::oilMaxValue,
            ProductionRowView::gasMinValue,
            ProductionRowView::gasMaxValue
        )
        .containsExactly(
            ProductionViewTestUtil.TOTAL_PROMPT,
            String.valueOf(expectedTotalDays),
            DecimalFormatUtils.bigDecimalToFormattedString(oilMinTotal),
            DecimalFormatUtils.bigDecimalToFormattedString(oilMaxTotal),
            DecimalFormatUtils.bigDecimalToFormattedString(gasMinTotal),
            DecimalFormatUtils.bigDecimalToFormattedString(gasMaxTotal)
        );

    // check averages
    assertThat(productionRowViews.get(productionMonths.size() + 1))
        .extracting(ProductionRowView::rowPrompt,
            ProductionRowView::consentDays,
            ProductionRowView::oilMinValue,
            ProductionRowView::oilMaxValue,
            ProductionRowView::gasMinValue,
            ProductionRowView::gasMaxValue
        )
        .containsExactly(
            ProductionViewTestUtil.AVERAGE_PROMPT.formatted(ProductionUnit.KSCM_PER_DAY.getDisplayName()),
            null,
            DecimalFormatUtils.bigDecimalToFormattedString(
                oilMinTotal.divide(BigDecimal.valueOf(expectedTotalDays), MAX_DECIMAL_PLACES, RoundingMode.HALF_UP)
            ),
            DecimalFormatUtils.bigDecimalToFormattedString(
                oilMaxTotal.divide(BigDecimal.valueOf(expectedTotalDays), MAX_DECIMAL_PLACES, RoundingMode.HALF_UP)
            ),
            DecimalFormatUtils.bigDecimalToFormattedString(
                gasMinTotal.divide(BigDecimal.valueOf(expectedTotalDays), MAX_DECIMAL_PLACES, RoundingMode.HALF_UP)
            ),
            DecimalFormatUtils.bigDecimalToFormattedString(
                gasMaxTotal.divide(BigDecimal.valueOf(expectedTotalDays), MAX_DECIMAL_PLACES, RoundingMode.HALF_UP)
            )
        );
  }
}
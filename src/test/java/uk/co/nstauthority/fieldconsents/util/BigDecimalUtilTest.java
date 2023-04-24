package uk.co.nstauthority.fieldconsents.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;

class BigDecimalUtilTest {

  private List<AnnualProductionMonth> annualProductionMonths;
  private List<BigDecimal> oilMinValues;
  private List<BigDecimal> oilMaxValues;
  private List<BigDecimal> gasMinValues;
  private List<BigDecimal> gasMaxValues;
  private final BigDecimal expectedOilMinSum = BigDecimal.valueOf(6);
  private final BigDecimal expectedOilMaxSum = BigDecimal.valueOf(27.6);
  private final BigDecimal expectedGasMinSum = BigDecimal.valueOf(20.64);
  private final BigDecimal expectedGasMaxSum = BigDecimal.valueOf(51);

  @BeforeEach
  void setUp() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    annualProductionMonths = ProductionTestUtils.getAnnualProductionMonthsData(applicationVersion);
    oilMinValues = annualProductionMonths.stream().map(ProductionRow::getOilMinValue).toList();
    oilMaxValues = annualProductionMonths.stream().map(ProductionRow::getOilMaxValue).toList();
    gasMinValues = annualProductionMonths.stream().map(ProductionRow::getGasMinValue).toList();
    gasMaxValues = annualProductionMonths.stream().map(ProductionRow::getGasMaxValue).toList();
  }

  @Test
  void sum_arrayOfValues() {
    assertThat(BigDecimalUtil.sum(
        oilMinValues.get(0), oilMinValues.get(1), oilMinValues.get(2), oilMinValues.get(3), oilMinValues.get(4),
        oilMinValues.get(5), oilMinValues.get(6), oilMinValues.get(7), oilMinValues.get(8), oilMinValues.get(9),
        oilMinValues.get(10), oilMinValues.get(11)
    )).isEqualByComparingTo(expectedOilMinSum);

    assertThat(BigDecimalUtil.sum(
        oilMaxValues.get(0), oilMaxValues.get(1), oilMaxValues.get(2), oilMaxValues.get(3), oilMaxValues.get(4),
        oilMaxValues.get(5), oilMaxValues.get(6), oilMaxValues.get(7), oilMaxValues.get(8), oilMaxValues.get(9),
        oilMaxValues.get(10), oilMaxValues.get(11)
    )).isEqualByComparingTo(expectedOilMaxSum);

    assertThat(BigDecimalUtil.sum(
        gasMinValues.get(0), gasMinValues.get(1), gasMinValues.get(2), gasMinValues.get(3), gasMinValues.get(4),
        gasMinValues.get(5), gasMinValues.get(6), gasMinValues.get(7), gasMinValues.get(8), gasMinValues.get(9),
        gasMinValues.get(10), gasMinValues.get(11)
    )).isEqualByComparingTo(expectedGasMinSum);

    assertThat(BigDecimalUtil.sum(
        gasMaxValues.get(0), gasMaxValues.get(1), gasMaxValues.get(2), gasMaxValues.get(3), gasMaxValues.get(4),
        gasMaxValues.get(5), gasMaxValues.get(6), gasMaxValues.get(7), gasMaxValues.get(8), gasMaxValues.get(9),
        gasMaxValues.get(10), gasMaxValues.get(11)
    )).isEqualByComparingTo(expectedGasMaxSum);
  }

  @Test
  void sum() {
    assertThat(BigDecimalUtil.sum(oilMinValues)).isEqualByComparingTo(expectedOilMinSum);
    assertThat(BigDecimalUtil.sum(oilMaxValues)).isEqualByComparingTo(expectedOilMaxSum);
    assertThat(BigDecimalUtil.sum(gasMinValues)).isEqualByComparingTo(expectedGasMinSum);
    assertThat(BigDecimalUtil.sum(gasMaxValues)).isEqualByComparingTo(expectedGasMaxSum);
  }

  @Test
  void sum_withValueMapper() {
    assertThat(BigDecimalUtil.sum(annualProductionMonths, ProductionRow::getOilMinValue))
        .isEqualByComparingTo(expectedOilMinSum);
    assertThat(BigDecimalUtil.sum(annualProductionMonths, ProductionRow::getOilMaxValue))
        .isEqualByComparingTo(expectedOilMaxSum);
    assertThat(BigDecimalUtil.sum(annualProductionMonths, ProductionRow::getGasMinValue))
        .isEqualByComparingTo(expectedGasMinSum);
    assertThat(BigDecimalUtil.sum(annualProductionMonths, ProductionRow::getGasMaxValue))
        .isEqualByComparingTo(expectedGasMaxSum);
  }

  @ParameterizedTest
  @MethodSource("getDivideRoundArguments")
  void divideRound(BigDecimal bigDecimal, int divisor, BigDecimal expectedBigDecimal) {
    assertThat(BigDecimalUtil.divideRound(bigDecimal, divisor))
        .isEqualByComparingTo(expectedBigDecimal);
  }

  private static Stream<Arguments> getDivideRoundArguments() {
    return Stream.of(
        Arguments.of(BigDecimal.valueOf(144.00000009), 12, BigDecimal.valueOf(12)),
        Arguments.of(BigDecimal.valueOf(144.000012), 12, BigDecimal.valueOf(12.000001)),
        Arguments.of(BigDecimal.valueOf(12.12), 12, BigDecimal.valueOf(1.01)),
        Arguments.of(BigDecimal.valueOf(12.000001), 2, BigDecimal.valueOf(6.000001)),
        Arguments.of(BigDecimal.valueOf(2.222229), 2, BigDecimal.valueOf(1.111115)),
        Arguments.of(BigDecimal.valueOf(9.999999999), 9, BigDecimal.valueOf(1.111111)),
        Arguments.of(BigDecimal.valueOf(3.333333333333), 3, BigDecimal.valueOf(1.111111))
    );
  }
}
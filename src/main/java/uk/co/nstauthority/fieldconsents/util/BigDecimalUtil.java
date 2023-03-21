package uk.co.nstauthority.fieldconsents.util;

import static uk.co.nstauthority.fieldconsents.validation.ValidatorUtils.MAX_DECIMAL_PLACES;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BigDecimalUtil {
  private BigDecimalUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static BigDecimal sum(BigDecimal... bigDecimals) {
    return BigDecimalUtil.sum(Arrays.stream(bigDecimals).toList());
  }

  public static BigDecimal sum(List<BigDecimal> bigDecimals) {
    return BigDecimalUtil.sum(bigDecimals, Function.identity());
  }

  public static <T> BigDecimal sum(List<T> values, Function<T, BigDecimal> valueMapper) {
    return values.stream().map(valueMapper).reduce(BigDecimal.ZERO, BigDecimal::add);
  }


  public static BigDecimal divideRound(BigDecimal bigDecimal, int divisor) {
    return bigDecimal.divide(BigDecimal.valueOf(divisor), MAX_DECIMAL_PLACES, RoundingMode.HALF_UP);
  }
}

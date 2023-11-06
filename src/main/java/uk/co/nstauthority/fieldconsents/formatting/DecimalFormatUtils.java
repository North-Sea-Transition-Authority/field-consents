package uk.co.nstauthority.fieldconsents.formatting;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class DecimalFormatUtils {

  public static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.##########");
  private static final NumberFormat MONEY_FORMAT = NumberFormat.getCurrencyInstance(Locale.UK);

  private DecimalFormatUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String doubleToFormattedString(Double value) {
    return value != null ? DEFAULT_DECIMAL_FORMAT.format(value) : "";
  }

  public static String bigDecimalToFormattedString(BigDecimal value) {
    return value != null ? DEFAULT_DECIMAL_FORMAT.format(value) : "";
  }

  public static String formatMoney(double amount) {
    return MONEY_FORMAT.format(amount);
  }
}

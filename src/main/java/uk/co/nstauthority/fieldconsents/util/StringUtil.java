package uk.co.nstauthority.fieldconsents.util;

import java.util.List;

public class StringUtil {

  private StringUtil() {
    throw new IllegalStateException("StringUtil is a utility class and cannot be instantiated");
  }

  public static String formatStringList(List<String> list) {
    if (list.isEmpty()) {
      return "";
    }
    if (list.size() == 1) {
      return list.get(0);
    }
    return "%s and %s".formatted(String.join(", ", list.subList(0, list.size() - 1)), list.get(list.size() - 1));
  }
}

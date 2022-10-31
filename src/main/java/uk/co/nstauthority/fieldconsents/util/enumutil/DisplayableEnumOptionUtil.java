package uk.co.nstauthority.fieldconsents.util.enumutil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public class DisplayableEnumOptionUtil {

  private DisplayableEnumOptionUtil() {
    throw new IllegalStateException("DisplayableEnumOptionUtil is a util class and should not be instantiated");
  }

  public static Map<String, String> getDisplayableOptions(
      Class<? extends Displayable> displayableOptionEnum
  ) {
    return Arrays.stream((Displayable[]) displayableOptionEnum.getEnumConstants())
        .sorted(Comparator.comparingInt(Displayable::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Displayable::getEnumName, Displayable::getDisplayName));
  }
}
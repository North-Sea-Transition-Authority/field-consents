package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum GeographicArea implements Displayable {
  CNS("Central North Sea", 1),
  IS("Irish Sea", 2),
  LAND("Land", 3),
  NNS("Northern North Sea", 4),
  SNS("Southern North Sea", 5),
  WOS("West of Shetland", 6),
  UNKNOWN("Unknown", 7);

  private final String displayName;

  private final int displayOrder;

  GeographicArea(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Map<String, String> getDisplayableOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptions(GeographicArea.class);
  }
}

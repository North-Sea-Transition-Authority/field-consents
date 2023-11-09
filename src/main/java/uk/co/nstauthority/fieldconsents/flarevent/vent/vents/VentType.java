package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum VentType implements Displayable {
  HP_VENT("HP vent system", 10),
  LP_VENT("LP vent system", 20),
  OTHER_VENT("Other vent system", 30);

  private final String displayName;
  private final int displayOrder;

  VentType(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  static Map<String, String> getAllAsMap() {
    return DisplayableEnumOptionUtil.getDisplayableOptions(VentType.class);
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getEnumName() {
    return this.name();
  }

}

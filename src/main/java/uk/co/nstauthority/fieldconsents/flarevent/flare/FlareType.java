package uk.co.nstauthority.fieldconsents.flarevent.flare;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum FlareType implements Displayable {
  HP_FLARE("HP Flare", 10),
  MP_FLARE("MP Flare", 20),
  LP_FLARE("LP Flare", 30),
  LPP_FLARE("LPP Flare", 40);

  private final String displayName;
  private final int displayOrder;

  FlareType(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  static Map<String, String> getAllAsMap() {
    return DisplayableEnumOptionUtil.getDisplayableOptions(FlareType.class);
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

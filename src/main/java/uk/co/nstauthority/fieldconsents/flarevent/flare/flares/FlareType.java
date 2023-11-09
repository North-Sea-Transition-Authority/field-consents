package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum FlareType implements Displayable {
  HP_FLARE("HP flare system", 10),
  MP_FLARE("MP flare system", 20),
  LP_FLARE("LP flare system", 30),
  LLP_FLARE("LLP flare system", 40);

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

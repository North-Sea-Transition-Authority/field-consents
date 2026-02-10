package uk.co.nstauthority.fieldconsents.search;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum AceFlagStatus implements Displayable {
  ACE("ACE", 10, true),
  NON_ACE("Non ACE", 20, false);

  private final String displayName;

  private final int displayOrder;

  private final boolean isAceApplication;

  AceFlagStatus(String displayName, int displayOrder, boolean isAceApplication) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.isAceApplication = isAceApplication;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return this.displayOrder;
  }

  public boolean isAceApplication() {
    return this.isAceApplication;
  }

  public static Map<String, String> getDisplayableOptions() {
    return Arrays.stream(AceFlagStatus.values())
        .sorted(Comparator.comparingInt(AceFlagStatus::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, AceFlagStatus::getDisplayName));
  }
}

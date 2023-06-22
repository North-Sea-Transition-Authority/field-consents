package uk.co.nstauthority.fieldconsents.application;

import java.util.Map;
import java.util.stream.Stream;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum ApplicationVersionStatus implements Displayable {
  IN_PROGRESS("In progress", 10),
  SUBMITTED("Submitted", 20),
  COMPLETED("Completed", 30),
  DELETED("Deleted", 40),
  WITHDRAWN("Withdrawn", 50);

  private final String displayName;
  private final int displayOrder;

  ApplicationVersionStatus(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public static Map<String, String> getWorkAreaOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptionsFromStream(
        Stream.of(
            IN_PROGRESS,
            SUBMITTED
        )
    );
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }
}

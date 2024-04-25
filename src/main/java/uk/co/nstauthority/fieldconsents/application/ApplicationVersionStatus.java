package uk.co.nstauthority.fieldconsents.application;

import java.util.Map;
import java.util.stream.Stream;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum ApplicationVersionStatus implements Displayable {
  IN_PROGRESS("In progress", 10),
  AWAITING_PAYMENT("Awaiting payment", 20),
  SUBMITTED("Submitted", 30),
  CONSENTED("Consented", 40),
  DELETED("Deleted", 50),
  WITHDRAWN("Withdrawn", 60);

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
            AWAITING_PAYMENT,
            SUBMITTED
        )
    );
  }

  public static Map<String, String> getSearchOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptionsFromStream(
        Stream.of(
            IN_PROGRESS,
            AWAITING_PAYMENT,
            SUBMITTED,
            CONSENTED,
            WITHDRAWN
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

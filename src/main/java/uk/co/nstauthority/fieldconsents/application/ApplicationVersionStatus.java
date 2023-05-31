package uk.co.nstauthority.fieldconsents.application;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum ApplicationVersionStatus implements Displayable {
  IN_PROGRESS("In progress", 10),
  SUBMITTED("Submitted", 20),
  COMPLETED("Completed", 30),
  DELETED("Deleted", 40);

  private final String displayName;
  private final int displayOrder;

  ApplicationVersionStatus(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public static Map<String, String> getDisplayableOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptions(ApplicationVersionStatus.class);
  }

  public static Map<String, String> getWorkAreaOptions() {
    var displayableOptions = ApplicationVersionStatus.getDisplayableOptions();
    displayableOptions.remove(COMPLETED.getEnumName());
    displayableOptions.remove(DELETED.getEnumName());
    return displayableOptions;
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

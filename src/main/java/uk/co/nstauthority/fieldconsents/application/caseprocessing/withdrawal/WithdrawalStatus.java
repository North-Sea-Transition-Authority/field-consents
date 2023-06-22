package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import java.util.Map;
import java.util.stream.Stream;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum WithdrawalStatus implements Displayable {
  OPEN("Open", 1),
  ACCEPTED("Accept", 2),
  REJECTED("Reject", 3);

  private final String displayName;

  private final int displayOrder;

  WithdrawalStatus(String displayName, int displayOrder) {
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

  public static Map<String, String> getWithdrawalResponseOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptionsFromStream(
        Stream.of(
            ACCEPTED,
            REJECTED
        )
    );
  }
}

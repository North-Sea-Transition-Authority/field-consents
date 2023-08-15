package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum ApplicationUpdateResponseType implements Displayable {
  REQUESTED_CHANGES_ONLY(
      "Requested changes only",
      1,
      null,
      null
  ),
  OTHER_CHANGES(
      "Other changes",
      2,
      "Describe the changes that have been made",
      "Provide a brief summary of the updates made to the application"
  );

  private final String displayName;

  private final int displayOrder;

  private final String responseTextLabel;

  private final String responseTextLabelHint;

  ApplicationUpdateResponseType(String displayName,
                                int displayOrder,
                                String responseTextLabel,
                                String responseTextLabelHint) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.responseTextLabel = responseTextLabel;
    this.responseTextLabelHint = responseTextLabelHint;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getResponseTextLabel() {
    return responseTextLabel;
  }

  public String getResponseTextLabelHint() {
    return responseTextLabelHint;
  }

  public static Map<String, String> getDisplayableOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptions(ApplicationUpdateResponseType.class);
  }
}

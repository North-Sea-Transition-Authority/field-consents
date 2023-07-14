package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum ApplicationUpdateResponseType implements Displayable {
  UPDATED_EXACTLY(
      "Updated application exactly as requested",
      1,
      null,
      null
  ),
  UPDATED(
      "Updated application as follows",
      2,
      "Update summary",
      "Provide a brief summary of the updates made to the application"
  ),
  NOT_UPDATED(
      "Not updated application",
      3,
      "Reason for not updating the application",
      "Provide details of why the application has not been updated"
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

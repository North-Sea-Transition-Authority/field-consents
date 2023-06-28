package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum TechnicalReviewResponseType implements Displayable {
  APPROVE(
      "Approve",
      1,
      "Consent conditions",
      "Provide consent conditions if they apply"
  ),
  REJECT(
      "Reject",
      2,
      "Reject reason",
      "Why are you rejecting this application"
  );

  private final String displayName;

  private final int displayOrder;

  private final String responseTextLabel;

  private final String responseTextLabelHint;

  TechnicalReviewResponseType(String displayName,
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
    return DisplayableEnumOptionUtil.getDisplayableOptions(TechnicalReviewResponseType.class);
  }
}

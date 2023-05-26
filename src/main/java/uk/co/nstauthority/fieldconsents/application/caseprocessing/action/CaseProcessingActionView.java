package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class CaseProcessingActionView {
  private final String displayName;
  private final int displayOrder;
  private final boolean primaryAction;
  private final String postUrl;
  private final String redirectUrl;

  CaseProcessingActionView(String displayName, int displayOrder, boolean primaryAction, String postUrl, String redirectUrl) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.primaryAction = primaryAction;
    this.postUrl = postUrl;
    this.redirectUrl = redirectUrl;
  }

  public static CaseProcessingActionView from(CaseProcessingActionItem actionItem, ApplicationVersion applicationVersion) {
    return new CaseProcessingActionView(
        actionItem.getDisplayName(),
        actionItem.getDisplayOrder(),
        actionItem.isPrimaryAction(),
        actionItem.getActionPostUrl(applicationVersion.getApplication().getId()),
        actionItem.getActionRedirectUrl(applicationVersion.getApplication().getId())
    );
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public boolean isPrimaryAction() {
    return primaryAction;
  }

  public String getPostUrl() {
    return postUrl;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }
}

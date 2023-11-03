package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public record CaseProcessingActionView(
    String displayName,
    boolean primaryAction,
    String postUrl,
    String redirectUrl
) {

  public static CaseProcessingActionView from(CaseProcessingActionItem actionItem, ApplicationVersion applicationVersion) {
    return new CaseProcessingActionView(
        actionItem.getDisplayName(),
        actionItem.isPrimaryAction(),
        actionItem.getActionPostUrl(applicationVersion.getApplication().getId()),
        actionItem.getActionRedirectUrl(applicationVersion.getApplication().getId())
    );
  }
}

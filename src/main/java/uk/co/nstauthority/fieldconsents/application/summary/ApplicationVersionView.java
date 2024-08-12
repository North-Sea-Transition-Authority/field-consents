package uk.co.nstauthority.fieldconsents.application.summary;

import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record ApplicationVersionView(
    Integer id,
    String displayText
) {

  public static ApplicationVersionView from(ApplicationVersion applicationVersion) {
    return new ApplicationVersionView(
        applicationVersion.getId(),
        getDisplayText(applicationVersion)
    );
  }

  private static String getDisplayText(ApplicationVersion applicationVersion) {
    var displayText = "Version " + applicationVersion.getVersion();

    var submittedDate = applicationVersion.getSubmittedDateTime();
    if (submittedDate != null) {
      displayText = displayText + ": " + DateUtils.format(applicationVersion.getSubmittedDateTime(), DateUtils.SHORT_DATE);
    }

    var status = applicationVersion.getStatus();
    if (status == ApplicationVersionStatus.WITHDRAWN) {
      displayText = displayText + " (" + ApplicationVersionStatus.WITHDRAWN.getDisplayName().toLowerCase() + ")";
    }

    return displayText;
  }

}

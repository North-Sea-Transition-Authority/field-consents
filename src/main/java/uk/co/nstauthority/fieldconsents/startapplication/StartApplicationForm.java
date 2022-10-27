package uk.co.nstauthority.fieldconsents.startapplication;

import uk.co.nstauthority.fieldconsents.application.ApplicationType;

public class StartApplicationForm {

  private ApplicationType applicationType;

  public StartApplicationForm() {
  }

  public StartApplicationForm(ApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  public ApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(ApplicationType applicationType) {
    this.applicationType = applicationType;
  }
}

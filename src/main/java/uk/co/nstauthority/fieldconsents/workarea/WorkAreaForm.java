package uk.co.nstauthority.fieldconsents.workarea;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

public class WorkAreaForm {

  private String reference;
  private List<ApplicationVersionStatus> statuses;
  private List<ApplicationType> applicationTypes;
  private List<ConsentLengthType> durationTypes;

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public List<ApplicationVersionStatus> getStatuses() {
    return statuses;
  }

  public void setStatuses(List<ApplicationVersionStatus> statuses) {
    this.statuses = statuses;
  }

  public List<ApplicationType> getApplicationTypes() {
    return applicationTypes;
  }

  public void setApplicationTypes(
      List<ApplicationType> applicationTypes) {
    this.applicationTypes = applicationTypes;
  }

  public List<ConsentLengthType> getDurationTypes() {
    return durationTypes;
  }

  public void setDurationTypes(List<ConsentLengthType> durationTypes) {
    this.durationTypes = durationTypes;
  }

  public static WorkAreaForm from(WorkAreaFilter filter) {
    var form = new WorkAreaForm();
    form.setReference(filter.getReference());
    form.setStatuses(filter.getStatuses());
    form.setApplicationTypes(filter.getApplicationTypes());
    form.setDurationTypes(filter.getDurationTypes());
    return form;
  }
}

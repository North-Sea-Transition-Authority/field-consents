package uk.co.nstauthority.fieldconsents.workarea;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

@SessionAttributes({"workAreaFilter"})
public class WorkAreaFilter implements Serializable {

  @Serial
  private static final long serialVersionUID = 8791625085927579692L;

  // TODO: The basic filters will only contain application reference, status and application types for now.
  //       See JIRA backlog for other filter functionalities.
  private String reference;
  private List<ApplicationVersionStatus> statuses;
  private List<ApplicationType> applicationTypes;
  private List<ConsentLengthType> durationTypes;

  public String getReference() {
    return reference;
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

  public void clearFilter() {
    reference = null;
    statuses = null;
    applicationTypes = null;
    durationTypes = null;
  }

  public void update(WorkAreaForm form) {
    reference = form.getReference();
    statuses = form.getStatuses();
    applicationTypes = form.getApplicationTypes();
    durationTypes = form.getDurationTypes();
  }
}

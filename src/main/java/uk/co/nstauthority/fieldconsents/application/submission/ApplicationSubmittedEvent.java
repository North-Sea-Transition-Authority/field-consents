package uk.co.nstauthority.fieldconsents.application.submission;

import org.springframework.context.ApplicationEvent;

public class ApplicationSubmittedEvent extends ApplicationEvent {

  private final Integer applicationVersionId;

  public ApplicationSubmittedEvent(Object source, Integer applicationVersionId) {
    super(source);
    this.applicationVersionId = applicationVersionId;
  }

  public Integer getApplicationVersionId() {
    return applicationVersionId;
  }
}

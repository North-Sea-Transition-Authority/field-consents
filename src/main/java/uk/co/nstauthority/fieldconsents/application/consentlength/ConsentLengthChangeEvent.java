package uk.co.nstauthority.fieldconsents.application.consentlength;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

public class ConsentLengthChangeEvent extends ApplicationEvent {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentLengthChangeEvent.class);

  private final Integer applicationVersionId;

  public ConsentLengthChangeEvent(Object source, Integer applicationVersionId) {
    super(source);
    this.applicationVersionId = applicationVersionId;

    LOGGER.debug("Event Consent length change fired.");
  }

  public Integer getApplicationVersionId() {
    return applicationVersionId;
  }
}

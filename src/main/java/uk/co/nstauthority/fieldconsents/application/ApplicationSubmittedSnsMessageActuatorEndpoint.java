package uk.co.nstauthority.fieldconsents.application;

import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@WebEndpoint(id = "application-submitted-sns-message")
class ApplicationSubmittedSnsMessageActuatorEndpoint {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSnsService applicationSnsService;

  ApplicationSubmittedSnsMessageActuatorEndpoint(
      ApplicationVersionService applicationVersionService,
      ApplicationSnsService applicationSnsService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSnsService = applicationSnsService;
  }

  @WriteOperation
  ResponseEntity<Void> publishMessage(@Selector Integer applicationVersionId) {
    var applicationVersion = applicationVersionService.getApplicationVersionById(applicationVersionId);
    applicationSnsService.publishApplicationSubmittedSnsMessage(applicationVersion);
    return ResponseEntity.ok().build();
  }
}

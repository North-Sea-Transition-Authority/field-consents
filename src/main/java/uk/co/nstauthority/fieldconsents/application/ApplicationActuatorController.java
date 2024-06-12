package uk.co.nstauthority.fieldconsents.application;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Component
@RestControllerEndpoint(id = "applications")
class ApplicationActuatorController {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSnsService applicationSnsService;

  ApplicationActuatorController(
      ApplicationVersionService applicationVersionService,
      ApplicationSnsService applicationSnsService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSnsService = applicationSnsService;
  }

  @PostMapping("publish-epmq-message/application-version/{applicationVersionId}/submitted")
  ResponseEntity<Void> publishApplicationSubmittedSnsMessage(@PathVariable("applicationVersionId") Integer applicationVersionId) {
    var applicationVersion = applicationVersionService.getApplicationVersionById(applicationVersionId);
    applicationSnsService.publishApplicationSubmittedSnsMessage(applicationVersion);
    return ResponseEntity.ok().build();
  }
}

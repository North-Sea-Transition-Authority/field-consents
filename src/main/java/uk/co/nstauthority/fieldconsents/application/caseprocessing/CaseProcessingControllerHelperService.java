package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;

@Service
public class CaseProcessingControllerHelperService {

  private final ApplicationVersionService applicationVersionService;

  CaseProcessingControllerHelperService(ApplicationVersionService applicationVersionService) {
    this.applicationVersionService = applicationVersionService;
  }

  public ApplicationVersion getApplicationVersionForApplication(Application application, Integer requestedApplicationVersionId) {
    var requestedApplicationVersion = applicationVersionService.getApplicationVersionById(requestedApplicationVersionId);

    if (requestedApplicationVersion.getStatus() == ApplicationVersionStatus.DELETED) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    if (!requestedApplicationVersion.getApplication().getId().equals(application.getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    return requestedApplicationVersion;
  }

}

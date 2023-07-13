package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
class ApplicationUpdateService {

  private final ApplicationService applicationService;

  private final ApplicationDuplicationService applicationDuplicationService;

  @Autowired
  ApplicationUpdateService(ApplicationService applicationService,
                           ApplicationDuplicationService applicationDuplicationService) {
    this.applicationService = applicationService;
    this.applicationDuplicationService = applicationDuplicationService;
  }

  @Transactional
  public void startApplicationUpdate(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var newApplicationVersion = applicationService.startApplicationUpdate(applicationVersion, user);
    applicationDuplicationService.duplicateApplicationSections(applicationVersion, newApplicationVersion);
  }
}

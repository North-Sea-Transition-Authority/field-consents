package uk.co.nstauthority.fieldconsents.application.caseprocessing.revision;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
public class ApplicationRevisionService {

  private final ApplicationService applicationService;
  private final ApplicationSubmissionService applicationSubmissionService;
  private final ApplicationDuplicationService applicationDuplicationService;
  private final TeamService teamService;
  private final ApplicationUnitService applicationUnitService;

  ApplicationRevisionService(
      ApplicationService applicationService,
      ApplicationSubmissionService applicationSubmissionService,
      ApplicationDuplicationService applicationDuplicationService,
      TeamService teamService,
      ApplicationUnitService applicationUnitService
  ) {
    this.applicationService = applicationService;
    this.applicationSubmissionService = applicationSubmissionService;
    this.applicationDuplicationService = applicationDuplicationService;
    this.teamService = teamService;
    this.applicationUnitService = applicationUnitService;
  }

  @Transactional
  public Application startApplicationRevision(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var newApplicationVersion = applicationService.startApplicationRevision(applicationVersion, user);

    applicationDuplicationService.duplicateApplicationSections(applicationVersion, newApplicationVersion);

    if (!teamService.isIndustryUser(user)) {
      applicationSubmissionService.regulatorAutoSubmitApplication(newApplicationVersion, applicationVersion, user);
    }

    return newApplicationVersion.getApplication();
  }

  public boolean isRevisable(ApplicationVersion applicationVersion) {
    // Belt and braces to stop certain legacy cases from being revisable
    return !applicationUnitService.hasLegacyEmissionCategoryType(applicationVersion);
  }
}

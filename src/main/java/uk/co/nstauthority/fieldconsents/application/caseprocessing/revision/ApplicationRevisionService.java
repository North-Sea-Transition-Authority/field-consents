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
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ApplicationRevisionService {

  private final ApplicationService applicationService;
  private final ApplicationSubmissionService applicationSubmissionService;
  private final ApplicationDuplicationService applicationDuplicationService;
  private final ApplicationUnitService applicationUnitService;
  private final TeamQueryService teamQueryService;

  ApplicationRevisionService(
      ApplicationService applicationService,
      ApplicationSubmissionService applicationSubmissionService,
      ApplicationDuplicationService applicationDuplicationService,
      ApplicationUnitService applicationUnitService,
      TeamQueryService teamQueryService
  ) {
    this.applicationService = applicationService;
    this.applicationSubmissionService = applicationSubmissionService;
    this.applicationDuplicationService = applicationDuplicationService;
    this.applicationUnitService = applicationUnitService;
    this.teamQueryService = teamQueryService;
  }

  @Transactional
  public Application startApplicationRevision(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var newApplicationVersion = applicationService.startApplicationRevision(applicationVersion, user);

    applicationDuplicationService.duplicateApplicationSections(applicationVersion, newApplicationVersion);

    if (!teamQueryService.userIsMemberOfTeamType(user, TeamType.INDUSTRY)) {
      applicationSubmissionService.regulatorAutoSubmitApplication(newApplicationVersion, applicationVersion, user);
    }

    return newApplicationVersion.getApplication();
  }

  public boolean isRevisable(ApplicationVersion applicationVersion) {
    // Belt and braces to stop certain legacy cases from being revisable
    return !applicationUnitService.hasLegacyEmissionCategoryType(applicationVersion);
  }
}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.revision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ApplicationRevisionServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationSubmissionService applicationSubmissionService;

  @Mock
  private ApplicationDuplicationService applicationDuplicationService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private ApplicationRevisionService applicationRevisionService;

  @Test
  void startApplicationRevision_userIsIndustryUser() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var user = ServiceUserDetailTestUtil.Builder().build();

    var newApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    newApplicationVersion.setId(2);

    when(applicationService.startApplicationRevision(applicationVersion, user)).thenReturn(newApplicationVersion);
    when(teamQueryService.userIsMemberOfTeamType(user, TeamType.INDUSTRY)).thenReturn(true);

    applicationRevisionService.startApplicationRevision(applicationVersion, user);

    verify(applicationDuplicationService).duplicateApplicationSections(applicationVersion, newApplicationVersion);
    verify(applicationSubmissionService, never()).regulatorAutoSubmitApplication(any(), any(), any());
  }

  @Test
  void startApplicationRevision_userIsNotIndustryUser() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var user = ServiceUserDetailTestUtil.Builder().build();

    var newApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    newApplicationVersion.setId(2);

    when(applicationService.startApplicationRevision(applicationVersion, user)).thenReturn(newApplicationVersion);
    when(teamQueryService.userIsMemberOfTeamType(user, TeamType.INDUSTRY)).thenReturn(false);

    applicationRevisionService.startApplicationRevision(applicationVersion, user);

    verify(applicationDuplicationService).duplicateApplicationSections(applicationVersion, newApplicationVersion);
    verify(applicationSubmissionService).regulatorAutoSubmitApplication(newApplicationVersion, applicationVersion, user);
  }

  @Test
  void isRevisable_true() {
    var applicationVersion = new ApplicationVersion();
    when(applicationUnitService.hasLegacyEmissionCategoryType(applicationVersion))
        .thenReturn(false);

    assertThat(applicationRevisionService.isRevisable(applicationVersion))
        .isEqualTo(true);
  }

  @Test
  void isRevisable_false() {
    var applicationVersion = new ApplicationVersion();
    when(applicationUnitService.hasLegacyEmissionCategoryType(applicationVersion))
        .thenReturn(true);

    assertThat(applicationRevisionService.isRevisable(applicationVersion))
        .isEqualTo(false);
  }
}

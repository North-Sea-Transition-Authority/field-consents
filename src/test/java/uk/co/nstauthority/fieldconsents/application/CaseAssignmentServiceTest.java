package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private RegulatorTeamService regulatorTeamService;

  @InjectMocks
  private CaseAssignmentService caseAssignmentService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void assignCaseOfficer_whenNotInCaseOfficerRole_thenThrowException() {
    when(regulatorTeamService.isCaseOfficer(USER))
        .thenReturn(false);

    assertThatThrownBy(() -> caseAssignmentService.assignCaseOfficer(applicationVersion, USER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot assign case officer as user with wua id %s is not in a regulator case officer role"
            .formatted(USER.wuaId()));
  }

  @Test
  void assignCaseOfficer_whenInCaseOfficerRole_thenApplicationVersionCaseOfficerWuaUpdated() {
    when(regulatorTeamService.isCaseOfficer(USER))
        .thenReturn(true);

    caseAssignmentService.assignCaseOfficer(applicationVersion, USER);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    applicationVersion.setCaseOfficerWuaId(USER.wuaId());
    assertThat(applicationVersionArgumentCaptor.getValue())
        .isEqualTo(applicationVersion);
  }

  @Test
  void unassignCaseOfficer_thenApplicationVersionCaseOfficerWuaNulled() {
    caseAssignmentService.unassignCaseOfficer(applicationVersion);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    applicationVersion.setCaseOfficerWuaId(null);
    assertThat(applicationVersionArgumentCaptor.getValue())
        .isEqualTo(applicationVersion);
  }
}

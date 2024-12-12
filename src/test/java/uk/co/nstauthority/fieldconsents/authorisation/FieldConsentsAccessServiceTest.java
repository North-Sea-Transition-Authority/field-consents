package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class FieldConsentsAccessServiceTest {

  @Mock
  private ApplicationAccessService applicationAccessService;

  @Mock
  private AssetAccessService assetAccessService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private FieldConsentsAccessService fieldConsentsAccessService;

  private final ServiceUserDetail user = ServiceUserDetailTestUtil.Builder().build();
  private final ApplicationVersion applicationVersion = new ApplicationVersion();

  @BeforeEach
  void setUp() {
    applicationVersion.setPrimaryOperatorOuId(1);
    applicationVersion.setApplication(new Application());
  }

  @Test
  void userHasAnyRegulatorRole() {
    var regulatorRoles = Set.of(Role.CASE_OFFICER, Role.CASE_MANAGER, Role.ACCESS_MANAGER);

    when(teamQueryService.getStaticRoles(user, TeamType.REGULATOR)).thenReturn(regulatorRoles);

    assertThat(fieldConsentsAccessService.userHasAnyRegulatorRole(user, regulatorRoles)).isTrue();
  }

  @Test
  void userHasAnyRegulatorRole_invalidRegulatorRolePassedIn() {
    // Role.EDITOR is an invalid regulator role
    var roles = Set.of(Role.CASE_MANAGER, Role.INDUSTRY_ACCESS_MANAGER, Role.EDITOR);
    assertThatThrownBy(() -> fieldConsentsAccessService.userHasAnyRegulatorRole(user, roles))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
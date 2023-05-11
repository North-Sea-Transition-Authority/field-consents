package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class ApplicationAccessServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @InjectMocks
  private ApplicationAccessService applicationAccessService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void hasApplicationPermission_whenDoesntHasPermission_thenFalse() {
    when(organisationUnitPermissionService
        .hasOperatorPermission(
            USER,
            applicationVersion.getPrimaryOperatorOuId(),
            RolePermission.SUBMIT_FCS_APPLICATIONS))
        .thenReturn(false);

    assertThat(
        applicationAccessService
            .hasApplicationPermission(USER, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS)
    ).isFalse();
  }

  @Test
  void hasApplicationPermission_whenPermissionForTeam_thenTrue() {
    when(organisationUnitPermissionService
        .hasOperatorPermission(
            USER,
            applicationVersion.getPrimaryOperatorOuId(),
            RolePermission.SUBMIT_FCS_APPLICATIONS))
        .thenReturn(true);

    assertThat(
        applicationAccessService
            .hasApplicationPermission(USER, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS)
    ).isTrue();
  }
}

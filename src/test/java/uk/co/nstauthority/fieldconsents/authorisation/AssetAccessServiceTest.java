package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ExtendWith(MockitoExtension.class)
class AssetAccessServiceTest {

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private FieldEquityPartnerAccessService fieldEquityPartnerAccessService;

  @InjectMocks
  private AssetAccessService assetAccessService;

  @Mock
  private AssetWithOperatorJson assetWithOperatorJson;

  private final ServiceUserDetail userDetail = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void userHasAnyIndustryRole() {
    var requiredRoles = Set.of(Role.CREATOR, Role.EDITOR, Role.CONSENT_RECIPIENT);

    when(assetWithOperatorJson.operatorExists()).thenReturn(true);

    when(organisationUnitPermissionService.getUserRolesForOperator(userDetail, assetWithOperatorJson))
        .thenReturn(requiredRoles);

    assertThat(assetAccessService.userHasAnyIndustryRole(userDetail, assetWithOperatorJson, requiredRoles)).isTrue();
  }

  @Test
  void userHasAnyIndustryRole_withFieldEquityPartnerRole() {
    var fieldWithOperatorJson = mock(FieldWithOperatorJson.class);
    assetWithOperatorJson = fieldWithOperatorJson;

    when(assetWithOperatorJson.operatorExists()).thenReturn(true);

    when(organisationUnitPermissionService.getUserRolesForOperator(userDetail, assetWithOperatorJson))
        .thenReturn(Set.of(Role.CREATOR, Role.EDITOR));

    when(fieldEquityPartnerAccessService.userIsFieldEquityPartner(userDetail, fieldWithOperatorJson))
        .thenReturn(true);

    assertThat(assetAccessService.userHasAnyIndustryRole(userDetail, assetWithOperatorJson, Set.of(Role.CREATOR, Role.EDITOR, Role.CONSENT_RECIPIENT))).isTrue();
  }

  @Test
  void userHasAnyIndustryRole_withoutFieldEquityPartnerRole() {
    var fieldWithOperatorJson = mock(FieldWithOperatorJson.class);
    assetWithOperatorJson = fieldWithOperatorJson;

    when(assetWithOperatorJson.operatorExists()).thenReturn(true);

    when(organisationUnitPermissionService.getUserRolesForOperator(userDetail, assetWithOperatorJson))
        .thenReturn(Set.of(Role.CREATOR, Role.EDITOR));

    when(fieldEquityPartnerAccessService.userIsFieldEquityPartner(userDetail, fieldWithOperatorJson))
        .thenReturn(false);

    assertThat(assetAccessService.userHasAnyIndustryRole(userDetail, assetWithOperatorJson, Set.of(Role.CREATOR, Role.EDITOR, Role.CONSENT_RECIPIENT))).isTrue();
  }

  @Test
  void userHasAnyIndustryRole_operatorDoesNotExist() {
    when(assetWithOperatorJson.operatorExists()).thenReturn(false);

    assertThat(assetAccessService.userHasAnyIndustryRole(userDetail, assetWithOperatorJson, Set.of())).isFalse();
  }

  @Test
  void userHasAnyIndustryRole_nonIndustryRolePassedIn() {
    // Role.ALLOCATOR is an invalid industry role
    var roles = Set.of(Role.ALLOCATOR, Role.CREATOR, Role.EDITOR);
    assertThatThrownBy(() -> assetAccessService.userHasAnyIndustryRole(userDetail, assetWithOperatorJson, roles))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;

@ExtendWith(MockitoExtension.class)
class CaseProcessingTabServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private PermissionService permissionService;

  private CaseProcessingTabService caseProcessingTabService;

  @BeforeEach
  void setUp() {
    caseProcessingTabService = new CaseProcessingTabService(permissionService);
  }

  @Test
  void getTabsAvailableToUser_andAllTabsAllowed() {
    when(permissionService.hasPermission(USER, CaseProcessingTab.VIEW_APPLICATION.getRolePermissions())).thenReturn(true);
    when(permissionService.hasPermission(USER, CaseProcessingTab.CASE_HISTORY.getRolePermissions())).thenReturn(true);

    assertThat(caseProcessingTabService.getTabsAvailableToUser(USER)).containsExactly(
        CaseProcessingTab.VIEW_APPLICATION,
        CaseProcessingTab.CASE_HISTORY
    );
  }

  @Test
  void getTabsAvailableToUser_andNoTabsAllowed() {
    when(permissionService.hasPermission(USER, CaseProcessingTab.VIEW_APPLICATION.getRolePermissions())).thenReturn(false);
    when(permissionService.hasPermission(USER, CaseProcessingTab.CASE_HISTORY.getRolePermissions())).thenReturn(false);

    assertThat(caseProcessingTabService.getTabsAvailableToUser(USER)).isEmpty();
  }
}

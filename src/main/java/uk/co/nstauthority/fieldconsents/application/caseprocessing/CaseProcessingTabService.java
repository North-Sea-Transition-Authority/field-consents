package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;

@Service
public class CaseProcessingTabService {

  private final PermissionService permissionService;

  public CaseProcessingTabService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public List<CaseProcessingTab> getTabsAvailableToUser(ServiceUserDetail user) {
    return EnumSet.allOf(CaseProcessingTab.class)
        .stream()
        .filter(tab -> permissionService.hasPermission(user, tab.getRolePermissions()))
        .toList();
  }
}

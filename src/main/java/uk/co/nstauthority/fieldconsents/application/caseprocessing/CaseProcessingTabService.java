package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import java.util.Arrays;
import java.util.Comparator;
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
    return Arrays.stream(CaseProcessingTab.values())
        .filter(tab -> permissionService.hasPermission(user, tab.getRolePermissions()))
        .sorted(Comparator.comparing(CaseProcessingTab::getDisplayOrder))
        .toList();
  }
}

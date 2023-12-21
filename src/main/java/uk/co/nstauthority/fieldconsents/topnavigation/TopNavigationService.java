package uk.co.nstauthority.fieldconsents.topnavigation;

import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;

@Service
public class TopNavigationService {

  private final PermissionService permissionService;

  TopNavigationService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public List<TopNavigationItem> getTopNavigationItems(ServiceUserDetail user) {
    if (user == null) {
      return EnumSet.allOf(TopNavigationItem.class)
          .stream()
          .filter(item -> item.getRequiredRoles().isEmpty())
          .toList();
    }

    return EnumSet.allOf(TopNavigationItem.class)
        .stream()
        .filter(item -> item.getRequiredRoles().isEmpty() || permissionService.hasPermission(user, item.getRequiredRoles()))
        .toList();
  }
}

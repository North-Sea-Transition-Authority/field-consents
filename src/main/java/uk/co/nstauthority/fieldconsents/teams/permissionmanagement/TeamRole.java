package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import java.util.Set;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumWithDescription;

public interface TeamRole extends DisplayableEnumWithDescription {

  Set<RolePermission> getRolePermissions();

}

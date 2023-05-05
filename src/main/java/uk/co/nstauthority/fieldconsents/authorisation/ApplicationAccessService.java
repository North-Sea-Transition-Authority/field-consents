package uk.co.nstauthority.fieldconsents.authorisation;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class ApplicationAccessService {

  private final OrganisationUnitPermissionService organisationUnitPermissionService;

  @Autowired
  ApplicationAccessService(OrganisationUnitPermissionService organisationUnitPermissionService) {
    this.organisationUnitPermissionService = organisationUnitPermissionService;
  }

  public boolean hasApplicationPermission(ServiceUserDetail user,
                                          ApplicationVersion applicationVersion,
                                          Set<RolePermission> requiredPermissions) {

    return organisationUnitPermissionService
        .hasOperatorPermission(user, applicationVersion.getPrimaryOperatorOuId(), requiredPermissions);
  }
}

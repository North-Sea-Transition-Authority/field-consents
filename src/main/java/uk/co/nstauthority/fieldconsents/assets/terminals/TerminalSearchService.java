package uk.co.nstauthority.fieldconsents.assets.terminals;

import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalsProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalsWithOperatorProjectionRoot;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class TerminalSearchService {

  private final TerminalApi terminalApi;
  private final TeamService teamService;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;

  TerminalSearchService(
      TerminalApi terminalApi,
      TeamService teamService,
      OrganisationUnitPermissionService organisationUnitPermissionService
  ) {
    this.terminalApi = terminalApi;
    this.teamService = teamService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
  }

  public List<TerminalJson> searchTerminals(String terminalName, String requestPurpose) {
    return terminalApi.searchTerminals(terminalName,
            Boolean.TRUE,
            terminalsProjectionRoot,
            new RequestPurpose(requestPurpose))
        .stream()
        .map(TerminalJson::from)
        .toList();
  }

  public List<TerminalWithOperatorJson> searchTerminalsWithOperatorForUser(
      String terminalName,
      String requestPurpose,
      ServiceUserDetail user
  ) {
    var terminalWithOperatorJsons = terminalApi.searchTerminals(
            terminalName,
            true,
            terminalsWithOperatorProjectionRoot,
            new RequestPurpose(requestPurpose)
        )
        .stream()
        .map(TerminalWithOperatorJson::from)
        .toList();

    var userRegulatorTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS);

    // short circuit and return all found terminals if the user is a regulator with view permissions
    if (!userRegulatorTeamsWithPermission.isEmpty()) {
      return terminalWithOperatorJsons;
    }

    var userConsulteeTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS);

    // short circuit and return all found terminals if the user is a consultee with view permissions
    if (!userConsulteeTeamsWithPermission.isEmpty()) {
      return terminalWithOperatorJsons;
    }

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    return terminalWithOperatorJsons
        .stream()
        .filter(terminal ->
            terminal.operatorExists()
                && organisationUnitIdsUserHasPermissionFor.contains(terminal.getOperatorJson().organisationUnitId()))
        .toList();
  }
}

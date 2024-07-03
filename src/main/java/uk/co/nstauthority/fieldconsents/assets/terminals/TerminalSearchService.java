package uk.co.nstauthority.fieldconsents.assets.terminals;

import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalsProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalsWithOperatorProjectionRoot;

import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Terminal;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
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
  private final ApplicationAssetService applicationAssetService;

  TerminalSearchService(
      TerminalApi terminalApi,
      TeamService teamService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      ApplicationAssetService applicationAssetService
  ) {
    this.terminalApi = terminalApi;
    this.teamService = teamService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.applicationAssetService = applicationAssetService;
  }

  public List<TerminalJson> searchTerminals(String terminalName, String requestPurpose) {
    return searchTerminals(
        terminalName,
        terminalsProjectionRoot,
        new RequestPurpose(requestPurpose),
        TerminalJson::from
    );
  }

  public List<TerminalWithOperatorJson> searchTerminalsWithOperatorForUser(
      String terminalName,
      String requestPurpose,
      ServiceUserDetail user
  ) {
    var terminalWithOperatorJsons = searchTerminals(
        terminalName,
        terminalsWithOperatorProjectionRoot,
        new RequestPurpose(requestPurpose),
        TerminalWithOperatorJson::from
    );

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

  private <T> List<T> searchTerminals(
      String terminalName,
      TerminalsProjectionRoot query,
      RequestPurpose requestPurpose,
      Function<Terminal, T> mappingFunction
  ) {
    var inUseTerminalIds = applicationAssetService.getAllUniqueAssetIdsForAssetType(AssetType.TERMINAL);
    return terminalApi.searchTerminals(terminalName, null, query, requestPurpose)
        .stream()
        .filter(terminal ->
            inUseTerminalIds.contains(terminal.getTerminalId()) || Boolean.TRUE.equals(terminal.getTerminalActive())
        )
        .map(mappingFunction)
        .toList();
  }

}

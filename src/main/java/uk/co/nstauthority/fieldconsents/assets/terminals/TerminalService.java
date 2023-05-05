package uk.co.nstauthority.fieldconsents.assets.terminals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class TerminalService {

  private final TerminalApi terminalApi;

  private final TeamService teamService;

  private final OrganisationUnitPermissionService organisationUnitPermissionService;

  static final TerminalsProjectionRoot terminalsProjectionRoot =
      new TerminalsProjectionRoot()
          .terminalId()
          .terminalName()
          .terminalActive();
  static final TerminalsProjectionRoot terminalsWithOperatorProjectionRoot =
      terminalsProjectionRoot
          .terminalOperator().organisationUnitId().name().root();

  static final TerminalProjectionRoot terminalProjectionRoot =
      new TerminalProjectionRoot()
          .terminalId()
          .terminalName()
          .terminalActive();

  static final TerminalProjectionRoot terminalWithOperatorProjectionRoot =
      terminalProjectionRoot
          .terminalOperator().organisationUnitId().name().root();

  @Autowired
  public TerminalService(TerminalApi terminalApi,
                         TeamService teamService,
                         OrganisationUnitPermissionService organisationUnitPermissionService) {
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

  public List<TerminalWithOperatorJson> searchTerminalsWithOperator(String terminalName,
                                                                    String requestPurpose,
                                                                    ServiceUserDetail user) {
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS);
    var userRegulatorTeams =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, requiredPermissions);

    var terminalWithOperatorJsons = terminalApi.searchTerminals(
            terminalName,
            true,
            terminalsWithOperatorProjectionRoot,
            new RequestPurpose(requestPurpose)
        )
        .stream()
        .map(TerminalWithOperatorJson::from)
        .toList();

    if (!userRegulatorTeams.isEmpty()) {
      return terminalWithOperatorJsons;
    }

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, requiredPermissions)
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

  public Optional<TerminalJson> findTerminal(Integer terminalId, String requestPurpose) {
    return terminalApi.findTerminalById(terminalId,
            terminalProjectionRoot,
            new RequestPurpose(requestPurpose))
        .map(TerminalJson::from);
  }

  public Optional<TerminalWithOperatorJson> findTerminalWithOperator(Integer terminalId, String requestPurpose) {
    return terminalApi.findTerminalById(terminalId, terminalWithOperatorProjectionRoot, new RequestPurpose(requestPurpose))
        .map(TerminalWithOperatorJson::from);
  }

  public TerminalJson getTerminal(Integer terminalId, String requestPurpose) {
    return findTerminal(terminalId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Terminal not found for terminal id %s".formatted(terminalId)));
  }

  public TerminalWithOperatorJson getTerminalWithOperator(Integer terminalId, String requestPurpose) {
    return findTerminalWithOperator(terminalId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Terminal not found for terminal id %s".formatted(terminalId)));
  }
}

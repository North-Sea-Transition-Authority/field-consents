package uk.co.nstauthority.fieldconsents.assets.terminals;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class TerminalService {

  private final TerminalApi terminalApi;

  private final TeamService teamService;

  private final OrganisationUnitService organisationUnitService;

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
                         OrganisationUnitService organisationUnitService) {
    this.terminalApi = terminalApi;
    this.teamService = teamService;
    this.organisationUnitService = organisationUnitService;
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

  public List<TerminalWithOperatorJson> searchTerminalsWithOperatorForUser(String terminalName,
                                                                           String requestPurpose,
                                                                           ServiceUserDetail user) {
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
        organisationUnitService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS)
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

  // TODO: FCS-427 (remove n+1)
  public List<TerminalJson> getTerminals(List<Integer> terminalIds, String requestPurpose) {
    if (terminalIds.isEmpty()) {
      return Collections.emptyList();
    }

    return terminalIds
        .stream()
        .map(id -> terminalApi.findTerminalById(id, terminalProjectionRoot, new RequestPurpose(requestPurpose)))
        .flatMap(Optional::stream)
        .map(TerminalJson::from)
        .toList();
  }

  public TerminalWithOperatorJson getTerminalWithOperator(Integer terminalId, String requestPurpose) {
    return findTerminalWithOperator(terminalId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Terminal not found for terminal id %s".formatted(terminalId)));
  }

  // TODO: FCS-427 (remove n+1)
  public List<TerminalWithOperatorJson> findTerminalsWithOperator(List<Integer> terminalIds, String epaRequestPurpose) {
    if (terminalIds.isEmpty()) {
      return Collections.emptyList();
    }

    return terminalIds
        .stream()
        .map(id -> terminalApi.findTerminalById(id, terminalWithOperatorProjectionRoot, new RequestPurpose(epaRequestPurpose)))
        .flatMap(Optional::stream)
        .map(TerminalWithOperatorJson::from)
        .toList();
  }

}

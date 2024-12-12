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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class TerminalSearchService {

  private final TerminalApi terminalApi;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final ApplicationAssetService applicationAssetService;
  private final TeamQueryService teamQueryService;

  TerminalSearchService(
      TerminalApi terminalApi,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      ApplicationAssetService applicationAssetService,
      TeamQueryService teamQueryService
  ) {
    this.terminalApi = terminalApi;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.applicationAssetService = applicationAssetService;
    this.teamQueryService = teamQueryService;
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

    if (teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES)) {
      return terminalWithOperatorJsons;
    }

    if (teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE, RoleGroup.CONSULTEE_WITH_VIEWER_ROLES)) {
      return terminalWithOperatorJsons;
    }

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES)
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

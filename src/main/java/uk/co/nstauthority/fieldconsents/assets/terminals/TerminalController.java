package uk.co.nstauthority.fieldconsents.assets.terminals;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasAssetPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromTerminalController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/manage-asset/facilities/{terminalId}")
@HasAssetPermission(permissions = {RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS})
public class TerminalController {

  private final TerminalService terminalService;

  private final OrganisationUnitPermissionService organisationUnitPermissionService;

  @Autowired
  public TerminalController(TerminalService terminalService,
                            OrganisationUnitPermissionService organisationUnitPermissionService) {
    this.terminalService = terminalService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
  }

  @GetMapping
  public ModelAndView manageTerminal(@PathVariable Integer terminalId,
                                     ServiceUserDetail user) {
    var terminalJson =
        terminalService.getTerminalWithOperator(terminalId, "Get terminal details for management screen");

    var startApplicationEnabled = terminalJson.operatorExists()
        && organisationUnitPermissionService.hasOperatorPermission(
            user, terminalJson.getOperatorJson().organisationUnitId(), RolePermission.CREATE_FCS_APPLICATIONS);

    return new ModelAndView("fcs/assets/terminals")
        .addObject("terminalJson", terminalJson)
        // the below default interface methods aren't accessible within the Freemarker,
        // so we have to pass in individually here
        .addObject("operatorExists", terminalJson.operatorExists())
        .addObject("startApplicationEnabled", startApplicationEnabled)
        .addObject("operatorName", terminalJson.getOperatorName())
        .addObject("backLinkUrl", ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromTerminalController.class).getStartApplicationForm(terminalId))
        );
  }
}

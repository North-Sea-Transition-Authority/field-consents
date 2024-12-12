package uk.co.nstauthority.fieldconsents.assets.terminals;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.ManageAssetService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanViewAsset;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromTerminalController;

@Controller
@RequestMapping("/manage-asset/facilities/{terminalId}")
@UserCanViewAsset
public class TerminalController {

  private final TerminalService terminalService;
  private final ManageAssetService manageAssetService;
  private final AssetService assetService;

  TerminalController(
      TerminalService terminalService,
      ManageAssetService manageAssetService,
      AssetService assetService
  ) {
    this.terminalService = terminalService;
    this.manageAssetService = manageAssetService;
    this.assetService = assetService;
  }

  @GetMapping
  public ModelAndView manageTerminal(@PathVariable Integer terminalId, ServiceUserDetail user) {
    var terminalJson = terminalService.getTerminalWithOperator(terminalId, "Get terminal details for management screen");
    var startApplicationDecision = assetService.getStartApplicationDecisionForTerminal(user, () -> terminalJson);

    return new ModelAndView("fcs/assets/terminals")
        .addObject("terminalJson", terminalJson)
        .addObject("startApplicationDecision", startApplicationDecision)
        .addObject("operatorName", terminalJson.getOperatorName())
        .addObject("backLinkUrl", ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromTerminalController.class).getStartApplicationForm(terminalId, null))
        )
        .addObject("applicationDataItemViews", manageAssetService.getApplicationDataItemViews(terminalJson.getAssetKey(), user));
  }
}

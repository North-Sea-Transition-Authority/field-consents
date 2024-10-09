package uk.co.nstauthority.fieldconsents.assets;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldController;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalController;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("manage-asset")
@AccessibleByServiceUsers
public class ManageAssetController {

  private final AssetService assetService;

  @Autowired
  public ManageAssetController(AssetService assetService) {
    this.assetService = assetService;
  }

  @GetMapping("asset-selected")
  public ModelAndView manageAsset(@RequestParam String assetKey) {
    return AssetKey.parse(assetKey)
        .flatMap(assetService::findAsset)
        .map(assetJson -> switch (assetJson.getAssetType()) {
          case FIELD -> ReverseRouter.redirect(on(FieldController.class).manageField(assetJson.getId(), null));
          case TERMINAL -> ReverseRouter.redirect(on(TerminalController.class).manageTerminal(assetJson.getId(), null));
          default -> throw new UnsupportedOperationException("Cannot manage non field/terminal asset");
        })
        .orElse(ReverseRouter.redirect(on(AssetSelectionController.class).getAssetSelection()));
  }
}

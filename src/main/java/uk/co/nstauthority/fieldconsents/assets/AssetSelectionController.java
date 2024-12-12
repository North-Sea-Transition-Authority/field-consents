package uk.co.nstauthority.fieldconsents.assets;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.UserCanManageAssets;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/manage-asset")
@UserCanManageAssets
public class AssetSelectionController {

  public static final String ASSET_SELECTION_TITLE = "Manage fields or facilities";

  @GetMapping
  public ModelAndView getAssetSelection() {
    return getAssetSelectionModelAndView(AssetSelectionForm.empty());
  }

  @PostMapping
  public ModelAndView manageAsset(@Valid @ModelAttribute("form") AssetSelectionForm assetSelectionForm,
                                  BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return getAssetSelectionModelAndView(assetSelectionForm);
    }

    var assetKey = assetSelectionForm.getAssetKey()
        .map(AssetKey::toString)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

    return ReverseRouter.redirect(on(ManageAssetController.class).manageAsset(assetKey));
  }

  private ModelAndView getAssetSelectionModelAndView(AssetSelectionForm assetSelectionForm) {
    return new ModelAndView("fcs/assets/assetSelection")
        .addObject("pageTitle", ASSET_SELECTION_TITLE)
        .addObject("form", assetSelectionForm)
        .addObject("assetSearchUrl", ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser(null, null)));
  }
}

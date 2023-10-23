package uk.co.nstauthority.fieldconsents.assets;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/manage-asset")
@HasPermission(permissions = RolePermission.MANAGE_ASSETS)
public class AssetSelectionController {

  public static final String ASSET_SELECTION_TITLE = "Manage fields/facilities";

  @GetMapping
  public ModelAndView getAssetSelection() {
    return getAssetSelectionModelAndView(new AssetSelectionForm());
  }

  @PostMapping
  public ModelAndView manageAsset(@Valid @ModelAttribute("form") AssetSelectionForm assetSelectionForm,
                                  BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return getAssetSelectionModelAndView(assetSelectionForm);
    }

    return ReverseRouter.redirect(on(ManageAssetController.class).manageAsset(assetSelectionForm.getAssetKey()));
  }

  private ModelAndView getAssetSelectionModelAndView(AssetSelectionForm assetSelectionForm) {

    return new ModelAndView("fcs/assets/assetSelection")
        .addObject("pageTitle", ASSET_SELECTION_TITLE)
        .addObject("form", assetSelectionForm);
  }
}

package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.ManageAssetController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping({"/", "/work-area"})
public class WorkAreaController {

  @GetMapping
  public ModelAndView getWorkArea() {
    return getWorkAreaModelAndView(new AssetSelectionForm());
  }

  @PostMapping
  public ModelAndView manageAsset(@Valid @ModelAttribute("form") AssetSelectionForm assetSelectionForm,
                                  BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return getWorkAreaModelAndView(assetSelectionForm);
    }

    return ReverseRouter.redirect(on(ManageAssetController.class).manageAsset(assetSelectionForm.getAssetKey()));
  }

  private ModelAndView getWorkAreaModelAndView(AssetSelectionForm assetSelectionForm) {

    return new ModelAndView("fcs/workarea/workArea")
        .addObject("form", assetSelectionForm);
  }

}

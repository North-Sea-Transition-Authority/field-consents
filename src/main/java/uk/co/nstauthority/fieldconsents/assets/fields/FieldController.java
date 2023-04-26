package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;

@RestController
@RequestMapping("/manage-asset/fields/{fieldId}")
public class FieldController {

  private final FieldService fieldService;

  @Autowired
  public FieldController(FieldService fieldService) {
    this.fieldService = fieldService;
  }

  @GetMapping
  public ModelAndView manageField(@PathVariable Integer fieldId) {

    FieldWithOperatorAndLicencesJson fieldJson
        = fieldService.getFieldWithOperatorAndLicences(fieldId,
        "Get field details for management screen");

    return new ModelAndView("fcs/assets/fields")
        .addObject("fieldJson", fieldJson)
        // the below default interface methods aren't accessible within the Freemarker,
        // so we have to pass in individually here
        .addObject("operatorExists", fieldJson.operatorExists())
        .addObject("licencesExist", fieldJson.licencesExist())
        .addObject("startApplicationEnabled", fieldJson.operatorExists() && fieldJson.licencesExist())
        .addObject("operatorName", fieldJson.getOperatorName())
        .addObject("licences", fieldJson.getLicencesAsString())
        .addObject("backLinkUrl", ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromFieldController.class).getStartApplicationForm(fieldId))
        );
  }
}

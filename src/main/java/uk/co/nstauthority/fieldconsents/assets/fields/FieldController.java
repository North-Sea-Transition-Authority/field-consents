package uk.co.nstauthority.fieldconsents.assets.fields;

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
import uk.co.nstauthority.fieldconsents.authorisation.HasAssetPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/manage-asset/fields/{fieldId}")
@HasAssetPermission(permissions = {RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS})
public class FieldController {

  private final FieldService fieldService;
  private final ManageAssetService manageAssetService;
  private final AssetService assetService;

  FieldController(
      FieldService fieldService,
      ManageAssetService manageAssetService,
      AssetService assetService
  ) {
    this.fieldService = fieldService;
    this.manageAssetService = manageAssetService;
    this.assetService = assetService;
  }

  @GetMapping
  public ModelAndView manageField(@PathVariable Integer fieldId, ServiceUserDetail user) {
    var fieldJson = fieldService.getFieldWithOperatorAndLicences(fieldId, "Get field details for management screen");
    var startApplicationDecision = assetService.getStartApplicationDecisionForField(user, () -> fieldJson);

    return new ModelAndView("fcs/assets/fields")
        .addObject("fieldJson", fieldJson)
        .addObject("startApplicationDecision", startApplicationDecision)
        // the below default interface methods aren't accessible within the Freemarker,
        // so we have to pass in individually here
        .addObject("operatorName", fieldJson.getOperatorName())
        .addObject("licences", fieldJson.getLicencesAsString())
        .addObject("backLinkUrl", ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromFieldController.class).getStartApplicationForm(fieldId, null))
        )
        .addObject("applicationDataItemViews", manageAssetService.getApplicationDataItemViews(fieldJson.getAssetKey(), user));
  }
}

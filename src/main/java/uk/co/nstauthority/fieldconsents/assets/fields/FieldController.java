package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("/manage-asset/fields/{fieldId}")
@HasPermission(permissions = {RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS})
public class FieldController {

  private final FieldService fieldService;

  private final OrganisationUnitPermissionService organisationUnitPermissionService;

  @Autowired
  public FieldController(FieldService fieldService,
                         OrganisationUnitPermissionService organisationUnitPermissionService) {
    this.fieldService = fieldService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
  }

  @GetMapping
  public ModelAndView manageField(@PathVariable Integer fieldId,
                                  ServiceUserDetail user) {
    var fieldJson =
        fieldService.getFieldWithOperatorAndLicences(fieldId, "Get field details for management screen");

    var startApplicationEnabled =
        fieldJson.operatorExists()
        && fieldJson.licencesExist()
        && organisationUnitPermissionService.hasOperatorPermission(
            user, fieldJson.getOperatorJson().organisationUnitId(), RolePermission.CREATE_FCS_APPLICATIONS);

    return new ModelAndView("fcs/assets/fields")
        .addObject("fieldJson", fieldJson)
        // the below default interface methods aren't accessible within the Freemarker,
        // so we have to pass in individually here
        .addObject("operatorExists", fieldJson.operatorExists())
        .addObject("licencesExist", fieldJson.licencesExist())
        .addObject("startApplicationEnabled", startApplicationEnabled)
        .addObject("operatorName", fieldJson.getOperatorName())
        .addObject("licences", fieldJson.getLicencesAsString())
        .addObject("backLinkUrl", ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()))
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromFieldController.class).getStartApplicationForm(fieldId))
        );
  }
}

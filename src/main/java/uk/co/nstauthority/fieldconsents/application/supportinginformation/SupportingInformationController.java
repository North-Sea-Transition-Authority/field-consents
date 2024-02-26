package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.ERAP_SUPPORTING_INFORMATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/supporting-information")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class SupportingInformationController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final SupportingInformationService supportingInformationService;
  private final SupportingInformationFormValidator supportingInformationFormValidator;
  private final FileControllerHelperService fileControllerHelperService;

  @Autowired
  public SupportingInformationController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      SupportingInformationService supportingInformationService,
      SupportingInformationFormValidator supportingInformationFormValidator,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.supportingInformationService = supportingInformationService;
    this.supportingInformationFormValidator = supportingInformationFormValidator;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping
  public ModelAndView getSupportingInformationForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var supportingInformationForm = supportingInformationService.getSupportingInformationForm(applicationVersion);
    var modelAndView = getSupportingInformationModelAndView(applicationVersion, supportingInformationForm);

    modelAndView.addObject("form", supportingInformationForm);
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveSupportingInformation(@PathVariable Integer applicationId,
                                                @ModelAttribute("form") SupportingInformationForm form,
                                                BindingResult bindingResult) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    form.setApplicationVersion(applicationVersion);

    supportingInformationFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getSupportingInformationModelAndView(applicationVersion, form);
    }

    supportingInformationService.saveSupportingInformation(applicationVersion, form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  private ModelAndView getSupportingInformationModelAndView(ApplicationVersion applicationVersion,
                                                            SupportingInformationForm form) {
    var applicationId = applicationVersion.getApplication().getId();
    var fileUploadAttributes = fileControllerHelperService.fileUploadComponentAttributes(
        form.getDocuments(),
        SupportingInformationFileController.class,
        controller -> controller.download(applicationId, null, null),
        controller -> controller.delete(applicationId, null, null)
    );
    var applicationType = applicationService.getApplicationById(applicationId).getType();
    var applicationTypeString = switch (applicationType) {
      case VENT -> "venting";
      case FLARE -> "flaring";
      case PRODUCTION -> "production";
    };

    return new ModelAndView("fcs/application/supportingInformationForm")
        .addObject("form", form)
        .addObject("erapInformationAllowed", ERAP_SUPPORTING_INFORMATION.allowed(applicationType))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId)))
        .addObject("fileUploadAttributes", fileUploadAttributes)
        .addObject("applicationType", applicationTypeString);
  }

}

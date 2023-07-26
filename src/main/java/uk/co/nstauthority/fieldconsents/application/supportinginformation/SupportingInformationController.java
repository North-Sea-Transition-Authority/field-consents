package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
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
  private final FieldConsentsFileService fieldConsentsFileService;

  @Autowired
  public SupportingInformationController(ApplicationService applicationService,
                                         ApplicationVersionService applicationVersionService,
                                         SupportingInformationService supportingInformationService,
                                         SupportingInformationFormValidator supportingInformationFormValidator,
                                         FieldConsentsFileService fieldConsentsFileService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.supportingInformationService = supportingInformationService;
    this.supportingInformationFormValidator = supportingInformationFormValidator;
    this.fieldConsentsFileService = fieldConsentsFileService;
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
      // TODO: https://jira.fivium.co.uk/browse/FDS-460
      var descriptionsByFileId = FileUploadLibraryUtils.getFileDescriptionsByFileId(form.getSupportingDocuments());
      form.setSupportingDocuments(fieldConsentsFileService.getUploadedFileForms(descriptionsByFileId.keySet()));
      form.getSupportingDocuments().forEach(uploadedFileForm -> uploadedFileForm
          .setFileDescription(descriptionsByFileId.get(uploadedFileForm.getFileId())));
      return getSupportingInformationModelAndView(applicationVersion, form);
    }

    supportingInformationService.saveSupportingInformation(applicationVersion, form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  private ModelAndView getSupportingInformationModelAndView(ApplicationVersion applicationVersion,
                                                            SupportingInformationForm form) {
    var applicationId = applicationVersion.getApplication().getId();
    var modelAndView = new ModelAndView("fcs/application/supportingInformationForm");
    var applicationType = applicationService.getApplicationById(applicationId).getType();

    boolean erapInformationAllowed = ApplicationTypeFeature.ERAP_SUPPORTING_INFORMATION.allowed(applicationType);
    if (erapInformationAllowed) {
      modelAndView.addObject("applicationType", applicationType.equals(ApplicationType.FLARE) ? "flaring" : "venting");
    }

    modelAndView
        .addObject("form", form)
        .addObject("erapInformationAllowed", erapInformationAllowed)
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId)))
        .addObject("fileUploadAttributes", getFileAttributes(applicationVersion, form.getSupportingDocuments()));

    return modelAndView;
  }

  private FileUploadComponentAttributes getFileAttributes(ApplicationVersion applicationVersion,
                                                          List<UploadedFileForm> existingFiles) {
    var controller = SupportingInformationDocumentController.class;
    var applicationId = applicationVersion.getApplication().getId();

    return fieldConsentsFileService.fileUploadComponentAttributesBuilder()
        .withPath("form.supportingDocuments")
        .withUploadUrl(ReverseRouter.route(on(controller).upload(applicationId, null, null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(applicationId, null)))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(applicationId, null)))
        .withExistingFiles(existingFiles)
        .build();
  }

}

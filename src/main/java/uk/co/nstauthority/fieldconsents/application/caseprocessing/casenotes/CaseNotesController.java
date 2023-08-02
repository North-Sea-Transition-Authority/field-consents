package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.file.UnlinkedFileController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/add-case-note")
public class CaseNotesController {

  private final CaseNotesService caseNotesService;
  private final ApplicationService applicationService;
  private final CaseNoteFormValidator caseNoteFormValidator;
  private final ApplicationVersionService applicationVersionService;
  private final FieldConsentsFileService fieldConsentsFileService;

  CaseNotesController(CaseNotesService caseNotesService,
                      ApplicationService applicationService,
                      CaseNoteFormValidator caseNoteFormValidator,
                      ApplicationVersionService applicationVersionService,
                      FieldConsentsFileService fieldConsentsFileService) {
    this.caseNotesService = caseNotesService;
    this.applicationService = applicationService;
    this.caseNoteFormValidator = caseNoteFormValidator;
    this.applicationVersionService = applicationVersionService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @GetMapping
  @ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
  public ModelAndView getNewCaseNote(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var caseNoteForm = new CaseNoteForm();

    return getNewCaseNoteModelAndView(applicationVersion, caseNoteForm)
        .addObject("form", caseNoteForm);
  }

  private ModelAndView getNewCaseNoteModelAndView(ApplicationVersion applicationVersion, CaseNoteForm form) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    return new ModelAndView("fcs/application/addCaseNote")
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(applicationId, null)))
        .addObject("applicationReference", applicationReference)
        .addObject("fileUploadAttributes", fileUploadComponentAttributes(applicationId, form.getCaseNoteDocuments()));
  }

  @PostMapping
  @ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
  public ModelAndView submitNewCaseNote(@PathVariable Integer applicationId,
                                        @ModelAttribute("form") CaseNoteForm form,
                                        BindingResult bindingResult,
                                        ServiceUserDetail user,
                                        RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);

    caseNoteFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      var descriptionsByFileId = FileUploadLibraryUtils.getFileDescriptionsByFileId(form.getCaseNoteDocuments());

      // TODO: https://jira.fivium.co.uk/browse/FDS-460
      form.setCaseNoteDocuments(fieldConsentsFileService.getUploadedFileForms(descriptionsByFileId.keySet()));
      form.getCaseNoteDocuments().forEach(uploadedFileForm -> uploadedFileForm
          .setFileDescription(descriptionsByFileId.get(uploadedFileForm.getFileId())));
      return getNewCaseNoteModelAndView(applicationVersion, form);
    }

    caseNotesService.saveCaseNote(
        applicationVersion,
        form.getCaseNoteText().getInputValue(),
        form.getCaseNoteDocuments(),
        user
    );
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "New case note added");

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).getApplicationCaseProcessing(applicationId, null));
  }

  private FileUploadComponentAttributes fileUploadComponentAttributes(Integer applicationId,
                                                                      List<UploadedFileForm> uploadedFileForms) {
    return fieldConsentsFileService.fileUploadComponentAttributesBuilder()
        .withPath("form.caseNoteDocuments")
        .withUploadUrl(ReverseRouter.route(on(CaseNotesDocumentController.class).upload(applicationId, null, null)))
        .withDownloadUrl(ReverseRouter.route(on(UnlinkedFileController.class).download(null, null)))
        .withDeleteUrl(ReverseRouter.route(on(UnlinkedFileController.class).delete(null, null)))
        .withExistingFiles(uploadedFileForms)
        .build();
  }

}

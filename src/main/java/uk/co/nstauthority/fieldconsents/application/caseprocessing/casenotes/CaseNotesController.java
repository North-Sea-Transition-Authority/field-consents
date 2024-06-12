package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/add-case-note")
@ActionEndPoint(REGULATOR_ADD_CASE_NOTE)
public class CaseNotesController {

  private final CaseNotesService caseNotesService;
  private final ApplicationService applicationService;
  private final CaseNoteFormValidator caseNoteFormValidator;
  private final ApplicationVersionService applicationVersionService;
  private final FileControllerHelperService fileControllerHelperService;

  CaseNotesController(
      CaseNotesService caseNotesService,
      ApplicationService applicationService,
      CaseNoteFormValidator caseNoteFormValidator,
      ApplicationVersionService applicationVersionService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.caseNotesService = caseNotesService;
    this.applicationService = applicationService;
    this.caseNoteFormValidator = caseNoteFormValidator;
    this.applicationVersionService = applicationVersionService;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping
  public ModelAndView getNewCaseNote(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var caseNoteForm = new CaseNoteForm();

    return getNewCaseNoteModelAndView(applicationVersion, caseNoteForm)
        .addObject("form", caseNoteForm);
  }

  private ModelAndView getNewCaseNoteModelAndView(ApplicationVersion applicationVersion, CaseNoteForm form) {
    var applicationId = applicationVersion.getApplication().getId();

    var fileUploadAttributes = fileControllerHelperService.fileUploadComponentAttributes(
        form.getDocuments(),
        this.getClass(),
        controller -> controller.downloadFile(applicationId, null, null),
        controller -> controller.deleteFile(applicationId, null, null)
    );
    var captionTitle = applicationService.getApplicationReference(
        applicationVersion,
        applicationVersion.getApplication().getType().getDisplayName() + " application"
    );

    return new ModelAndView("fcs/application/addCaseNote")
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)))
        .addObject("captionTitle", captionTitle)
        .addObject("fileUploadAttributes", fileUploadAttributes);
  }

  @PostMapping
  public ModelAndView submitNewCaseNote(@PathVariable Integer applicationId,
                                        @ModelAttribute("form") CaseNoteForm form,
                                        BindingResult bindingResult,
                                        ServiceUserDetail user,
                                        RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);

    caseNoteFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getNewCaseNoteModelAndView(applicationVersion, form);
    }

    caseNotesService.saveCaseNote(
        applicationVersion,
        form.getCaseNoteText().getInputValue(),
        form.getDocuments(),
        user
    );
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "New case note added");

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).caseProcessing(applicationId, null, null));
  }

  @GetMapping("/files/{fileId}")
  ResponseEntity<InputStreamResource> downloadFile(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.download(
        fileId,
        this::throwUnlinkedCaseNoteFileUsageException,
        userDetail
    );
  }

  @PostMapping("/files/delete/{fileId}")
  ResponseEntity<FileDeleteResponse> deleteFile(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.delete(
        fileId,
        this::throwUnlinkedCaseNoteFileUsageException,
        userDetail
    );
  }

  private FieldConsentsFileUsage throwUnlinkedCaseNoteFileUsageException() {
    throw new UnsupportedOperationException("Usage lookup for unlinked file");
  }

}

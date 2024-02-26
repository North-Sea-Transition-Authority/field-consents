package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceControllerHelperService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-preparation/documents")
@ActionEndPoint(CaseProcessingActionItem.CONSENT_PREPARATION)
public class ConsentPreparationDocumentsController {

  private final ApplicationService applicationService;
  private final ConsentPreparationDocumentService consentDocumentService;
  private final ConsentPreparationSupportingDocumentsFormValidator consentSupportingDocumentsFormValidator;
  private final FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService;
  private final FieldConsentsFileService fieldConsentsFileService;
  private final FileService fileService;

  ConsentPreparationDocumentsController(
      ApplicationService applicationService,
      ConsentPreparationDocumentService consentDocumentService,
      ConsentPreparationSupportingDocumentsFormValidator consentPreparationSupportingDocumentsFormValidator,
      FieldConsentsDocumentInstanceControllerHelperService fieldConsentsDocumentInstanceControllerHelperService,
      FieldConsentsFileService fieldConsentsFileService,
      FileService fileService
  ) {
    this.applicationService = applicationService;
    this.consentDocumentService = consentDocumentService;
    this.consentSupportingDocumentsFormValidator = consentPreparationSupportingDocumentsFormValidator;
    this.fieldConsentsDocumentInstanceControllerHelperService = fieldConsentsDocumentInstanceControllerHelperService;
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.fileService = fileService;
  }

  @GetMapping
  public ModelAndView editDocuments(@PathVariable Integer applicationId) {
    var application = applicationService.getApplicationById(applicationId);
    var form = consentDocumentService.getConsentSupportingDocumentsForm(application);

    return getModelAndView(application, form);
  }

  @PostMapping
  public ModelAndView saveDocuments(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ConsentPreparationSupportingDocumentsForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);
    consentSupportingDocumentsFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(application, form);
    }

    consentDocumentService.saveSupportingConsentDocuments(application, form.getDocuments());

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Consent documents saved");

    return ReverseRouter.redirect(on(ConsentPreparationController.class).viewConsentPreparationPage(applicationId));
  }

  @GetMapping("{fileId}")
  ResponseEntity<InputStreamResource> download(@PathVariable Integer applicationId, @PathVariable UUID fileId) {
    var application = applicationService.getApplicationById(applicationId);
    var usage = ApplicationFileUsage.supportingConsentDocumentFrom(application);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> fieldConsentsFileService.getFileNotFoundException(fileId, usage));

    fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, usage);

    return fileService.download(uploadedFile);
  }

  private ModelAndView getModelAndView(Application application, ConsentPreparationSupportingDocumentsForm form) {
    var fileUploadAttributes = fieldConsentsFileService.fileUploadComponentAttributes(form.getDocuments());
    var documentInstanceSummaryViews =
        fieldConsentsDocumentInstanceControllerHelperService.getDocumentInstanceSummaryViews(application);

    return new ModelAndView("fcs/application/consent/documents/consentDocumentsForm")
        .addObject("documentInstanceSummaryViews", documentInstanceSummaryViews)
        .addObject("form", form)
        .addObject("cancelUrl",
            ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(application.getId())))
        .addObject("fileUploadAttributes", fileUploadAttributes);
  }
}

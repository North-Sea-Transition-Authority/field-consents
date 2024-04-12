package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceViewService;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-preparation/documents")
@ActionEndPoint(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS)
public class ConsentPreparationDocumentsController {

  private final ApplicationService applicationService;
  private final ConsentPreparationDocumentService consentDocumentService;
  private final ConsentPreparationSupportingDocumentsFormValidator consentSupportingDocumentsFormValidator;
  private final ApplicationDocumentInstanceViewService applicationDocumentInstanceViewService;
  private final FileControllerHelperService fileControllerHelperService;

  ConsentPreparationDocumentsController(
      ApplicationService applicationService,
      ConsentPreparationDocumentService consentDocumentService,
      ConsentPreparationSupportingDocumentsFormValidator consentPreparationSupportingDocumentsFormValidator,
      ApplicationDocumentInstanceViewService applicationDocumentInstanceViewService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.applicationService = applicationService;
    this.consentDocumentService = consentDocumentService;
    this.consentSupportingDocumentsFormValidator = consentPreparationSupportingDocumentsFormValidator;
    this.applicationDocumentInstanceViewService = applicationDocumentInstanceViewService;
    this.fileControllerHelperService = fileControllerHelperService;
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

    return ReverseRouter.redirect(on(ConsentPreparationController.class).viewConsentPreparationPage(applicationId, null));
  }

  private ModelAndView getModelAndView(Application application, ConsentPreparationSupportingDocumentsForm form) {
    var fileUploadAttributes = fileControllerHelperService.fileUploadComponentAttributes(
        form.getDocuments(),
        ConsentPreparationFileController.class,
        controller -> controller.download(application.getId(), null, null),
        controller -> controller.delete(application.getId(), null, null)
    );
    var documentInstanceSummaryViews =
        applicationDocumentInstanceViewService.getDocumentInstanceSummaryViews(application);

    return new ModelAndView("fcs/application/consent/documents/consentDocumentsForm")
        .addObject("documentInstanceSummaryViews", documentInstanceSummaryViews)
        .addObject("form", form)
        .addObject("cancelUrl",
            ReverseRouter.route(on(ConsentPreparationController.class).viewConsentPreparationPage(application.getId(), null)))
        .addObject("fileUploadAttributes", fileUploadAttributes);
  }
}

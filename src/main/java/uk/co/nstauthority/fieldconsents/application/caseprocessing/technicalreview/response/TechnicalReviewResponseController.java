package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_SUBMIT_REVIEW;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/technical-review-response")
@ActionEndPoint(TECHNICAL_REVIEWER_SUBMIT_REVIEW)
public class TechnicalReviewResponseController {

  private final TechnicalReviewService technicalReviewService;
  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final TechnicalReviewResponseFormValidator validator;
  private final ApplicationSummaryService applicationSummaryService;
  private final FileControllerHelperService fileControllerHelperService;

  TechnicalReviewResponseController(
      TechnicalReviewService technicalReviewService,
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      TechnicalReviewResponseFormValidator validator,
      ApplicationSummaryService applicationSummaryService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.technicalReviewService = technicalReviewService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.validator = validator;
    this.applicationSummaryService = applicationSummaryService;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping
  public ModelAndView getForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var technicalReview = technicalReviewService.getOpenTechnicalReview(applicationVersion);
    var form = TechnicalReviewResponseForm.empty();

    return getModelAndView(applicationVersion, technicalReview, form);
  }

  @PostMapping
  ModelAndView submitForm(@PathVariable Integer applicationId,
                          @ModelAttribute("form") TechnicalReviewResponseForm form,
                          BindingResult bindingResult,
                          ServiceUserDetail serviceUserDetail,
                          RedirectAttributes redirectAttributes) {
    validator.validate(form, bindingResult);

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var technicalReview = technicalReviewService.getOpenTechnicalReview(applicationVersion);

    if (bindingResult.hasErrors()) {
      return getModelAndView(applicationVersion, technicalReview, form);
    }

    technicalReviewService.saveTechnicalReviewResponse(
        applicationVersion,
        technicalReview,
        serviceUserDetail,
        form.responseType(),
        form.consentConditions().getInputValue(),
        form.rejectionReason().getInputValue(),
        form.documents()
    );

    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Technical review submitted for application %s".formatted(applicationReference)
    );

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  private ModelAndView getModelAndView(ApplicationVersion applicationVersion,
                                       TechnicalReview technicalReview,
                                       TechnicalReviewResponseForm form) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();
    var technicalReviewId = technicalReview.getId();

    var fileUploadAttributes = fileControllerHelperService.fileUploadComponentAttributes(
        form.documents(),
        TechnicalReviewResponseFileController.class,
        controller -> controller.download(applicationId, technicalReviewId, null, null),
        controller -> controller.delete(applicationId, technicalReviewId, null, null)
    );
    var backLinkUrl = ReverseRouter.route(on(ApplicationCaseProcessingController.class)
        .caseProcessing(applicationId, null, null));

    var modelAndView = new ModelAndView("fcs/application/review/technicalReviewResponse");

    modelAndView
        .addObject("form", form)
        .addObject("technicalReviewSummaryView", TechnicalReviewSummaryView.from(technicalReview))
        .addObject("applicationReference", applicationReference)
        .addObject("backLinkUrl", backLinkUrl)
        .addObject("approveRadio", TechnicalReviewResponseType.APPROVE)
        .addObject("rejectRadio", TechnicalReviewResponseType.REJECT)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);

    return modelAndView;
  }

}

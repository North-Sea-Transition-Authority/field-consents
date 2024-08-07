package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.BREACH_INFORMATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_BREACH_INFORMATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.RECORD_BREACH;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REMOVE_BREACH;

import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/breach-information")
public class ConsentBreachController {

  private final ConsentService consentService;
  private final ConsentBreachService consentBreachService;
  private final ConsentBreachSummaryService consentBreachSummaryService;
  private final ConsentBreachFormValidator consentBreachFormValidator;
  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final CaseProcessingActionService caseProcessingActionService;

  ConsentBreachController(
      ConsentService consentService,
      ConsentBreachService consentBreachService,
      ConsentBreachSummaryService consentBreachSummaryService,
      ConsentBreachFormValidator consentBreachFormValidator,
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      CaseProcessingActionService caseProcessingActionService
  ) {
    this.consentService = consentService;
    this.consentBreachService = consentBreachService;
    this.consentBreachSummaryService = consentBreachSummaryService;
    this.consentBreachFormValidator = consentBreachFormValidator;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.caseProcessingActionService = caseProcessingActionService;
  }

  @GetMapping
  @ActionEndPoint(BREACH_INFORMATION)
  public ModelAndView breachInformation(
      @PathVariable Integer applicationId,
      ServiceUserDetail user
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var captionTitle = applicationService.getApplicationReference(applicationVersion);
    var consentBreachView = consentBreachService.findConsentBreachByApplication(applicationVersion.getApplication())
        .map(consentBreachSummaryService::getConsentBreachView)
        .orElse(null);
    var breachInformationGroupActions = caseProcessingActionService.getUserActionViewsForGroup(
        applicationVersion, user, CaseProcessingActionGroup.BREACH_INFORMATION);
    var breachInformationCardGroupActions = caseProcessingActionService.getUserActionViewsForGroup(
        applicationVersion, user, CaseProcessingActionGroup.BREACH_INFORMATION_CARD);

    return new ModelAndView("fcs/application/consent/breaches/consentBreachInformation")
        .addObject("captionTitle", captionTitle)
        .addObject("consentBreachView", consentBreachView)
        .addObject("breachInformationGroupActions", breachInformationGroupActions)
        .addObject("breachInformationCardGroupActions", breachInformationCardGroupActions)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(applicationId, null, null, null)));
  }

  @GetMapping("update")
  @ActionEndPoint({RECORD_BREACH, EDIT_BREACH_INFORMATION})
  public ModelAndView getConsentBreachForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var consentBreach = consentBreachService.findConsentBreachByApplication(applicationVersion.getApplication());
    var consentBreachForm = consentBreach.map(ConsentBreachForm::from)
        .orElseGet(ConsentBreachForm::new);

    return getConsentBreachModelAndView(applicationVersion)
        .addObject("form", consentBreachForm);
  }

  private ModelAndView getConsentBreachModelAndView(ApplicationVersion applicationVersion) {
    var captionTitle = applicationService.getApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    return new ModelAndView("fcs/application/consent/breaches/consentBreachForm")
        .addObject("captionTitle", captionTitle)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ConsentBreachController.class).breachInformation(applicationId, null)));
  }

  @PostMapping("update")
  @ActionEndPoint({RECORD_BREACH, EDIT_BREACH_INFORMATION})
  ModelAndView saveConsentBreach(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ConsentBreachForm form,
      BindingResult bindingResult,
      ServiceUserDetail user,
      RedirectAttributes redirectAttributes
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    consentBreachFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getConsentBreachModelAndView(applicationVersion)
          .addObject("form", form);
    }

    var consent = consentService.getConsent(applicationVersion.getApplication());

    consentBreachService.saveConsentBreach(
        consent,
        form.getConsentBreachText().getInputValue(),
        user
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Breach recorded");

    return ReverseRouter.redirect(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null, null));
  }

  @GetMapping("/delete")
  @ActionEndPoint(REMOVE_BREACH)
  public ModelAndView getDeleteBreachConfirmation(
      @PathVariable Integer applicationId
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getDeleteBreachModelAndView(applicationVersion);
  }

  private ModelAndView getDeleteBreachModelAndView(ApplicationVersion applicationVersion) {
    var captionTitle = applicationService.getApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();
    var consentBreach = consentBreachService.getConsentBreachByApplication(applicationVersion.getApplication());
    var consentBreachView = consentBreachSummaryService.getConsentBreachView(consentBreach);

    return new ModelAndView("fcs/application/consent/breaches/deleteConsentBreachForm")
        .addObject("consentBreachView", consentBreachView)
        .addObject("captionTitle", captionTitle)
        .addObject("breachInformationCardGroupActions", Collections.emptyList())
        .addObject("cancelUrl",
            ReverseRouter.route(on(ConsentBreachController.class).breachInformation(applicationId, null)));
  }

  @PostMapping("/delete")
  @ActionEndPoint(REMOVE_BREACH)
  ModelAndView deleteBreach(
      @PathVariable Integer applicationId,
      RedirectAttributes redirectAttributes
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var consentBreach = consentBreachService.getConsentBreachByApplication(applicationVersion.getApplication());

    consentBreachService.deleteConsentBreach(consentBreach);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Breach removed");

    return ReverseRouter.redirect(on(ApplicationCaseProcessingController.class)
        .caseProcessing(applicationId, null, null, null));
  }
}

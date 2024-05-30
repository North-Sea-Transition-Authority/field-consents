package uk.co.nstauthority.fieldconsents.feedback;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping
@AccessibleByServiceUsers
public class FeedbackController {

  public static final String PAGE_NAME = "Feedback";
  public static final int MAX_FEEDBACK_CHARACTER_LENGTH = 2000;

  private final FeedbackService feedbackService;
  private final FeedbackFormValidator feedbackFormValidator;
  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final UserDetailService userDetailService;

  FeedbackController(FeedbackService feedbackService,
                     FeedbackFormValidator feedbackFormValidator,
                     ApplicationService applicationService,
                     ApplicationVersionService applicationVersionService,
                     UserDetailService userDetailService) {
    this.feedbackService = feedbackService;
    this.feedbackFormValidator = feedbackFormValidator;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.userDetailService = userDetailService;
  }

  @GetMapping("/feedback")
  public ModelAndView getFeedback(@ModelAttribute("form") FeedbackForm form) {
    return getFeedbackModelAndView(form);
  }

  @PostMapping("/feedback")
  public ModelAndView submitFeedback(@ModelAttribute("form") FeedbackForm form,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {

    feedbackFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFeedbackModelAndView(form);
    }

    feedbackService.saveFeedback(form.getServiceRating(), form.getFeedback().getInputValue(), userDetailService.getUserDetail());

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Your feedback has been submitted");

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  @GetMapping("/applications/{applicationId}/feedback")
  public ModelAndView getApplicationFeedback(@PathVariable Integer applicationId,
                                             @ModelAttribute("form") FeedbackForm form) {
    var application = applicationService.getApplicationById(applicationId);
    return getApplicationFeedbackModelAndView(form, application);
  }

  @PostMapping("/applications/{applicationId}/feedback")
  public ModelAndView submitApplicationFeedback(@PathVariable Integer applicationId,
                                                @ModelAttribute("form") FeedbackForm form,
                                                BindingResult bindingResult,
                                                RedirectAttributes redirectAttributes) {
    var application = applicationService.getApplicationById(applicationId);
    feedbackFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getApplicationFeedbackModelAndView(form, application);
    }

    feedbackService.saveFeedback(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        userDetailService.getUserDetail());

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Your feedback has been submitted");

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  private ModelAndView getBaseModelAndView(FeedbackForm feedbackForm) {
    return new ModelAndView("fcs/feedback/feedback")
        .addObject("form", feedbackForm)
        .addObject("pageName", PAGE_NAME)
        .addObject("maxCharacterLength", String.valueOf(MAX_FEEDBACK_CHARACTER_LENGTH))
        .addObject("serviceRatings",
            DisplayableEnumOptionUtil.getDisplayableOptions(ServiceFeedbackRating.class));
  }

  private ModelAndView getApplicationFeedbackModelAndView(FeedbackForm feedbackForm, Application application) {
    return getBaseModelAndView(feedbackForm)
        .addObject("actionUrl", ReverseRouter.route(on(FeedbackController.class)
            .submitApplicationFeedback(application.getId(), null, null, null)));
  }

  private ModelAndView getFeedbackModelAndView(FeedbackForm feedbackForm) {
    return getBaseModelAndView(feedbackForm)
        .addObject("actionUrl", ReverseRouter.route(on(FeedbackController.class)
            .submitFeedback(null, null, null)));
  }
}

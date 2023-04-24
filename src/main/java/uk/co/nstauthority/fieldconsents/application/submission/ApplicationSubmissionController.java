package uk.co.nstauthority.fieldconsents.application.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/submit")
public class ApplicationSubmissionController {

  public static final String PAGE_TITLE = "Application submitted";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSubmissionService applicationSubmissionService;


  public ApplicationSubmissionController(ApplicationService applicationService,
                                         ApplicationVersionService applicationVersionService,
                                         ApplicationSubmissionService applicationSubmissionService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSubmissionService = applicationSubmissionService;
  }

  @PostMapping
  public ModelAndView submitApplication(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (applicationSubmissionService.isSubmittable(applicationVersion)) {
      applicationService.submitApplication(applicationVersion);
    } else {
      throw new RuntimeException("The application with id %s cannot be submitted!".formatted(applicationId));
    }

    return applicationSubmittedModelAndView(applicationVersion);
  }

  private ModelAndView applicationSubmittedModelAndView(ApplicationVersion applicationVersion) {
    ModelAndView modelAndView = new ModelAndView("fcs/application/submissionConfirmation");
    modelAndView.addObject("pageTitle", PAGE_TITLE);
    modelAndView.addObject("applicationReference", applicationService.generateApplicationReference(applicationVersion));
    modelAndView.addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
    return modelAndView;
  }
}

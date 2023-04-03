package uk.co.nstauthority.fieldconsents.application.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/submit")
public class ApplicationSubmissionController {

  public static final String PAGE_TITLE = "Application submitted";
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSubmissionService applicationSubmissionService;


  public ApplicationSubmissionController(ApplicationVersionService applicationVersionService,
                                         ApplicationSubmissionService applicationSubmissionService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSubmissionService = applicationSubmissionService;
  }

  @PostMapping
  public ModelAndView submitApplication(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (applicationSubmissionService.isSubmittable(applicationVersion)) {
      applicationVersionService.submit(applicationVersion);
    } else {
      throw new RuntimeException("The application with id %s cannot be submitted!".formatted(applicationId));
    }

    return applicationSubmittedModelAndView();
  }

  private ModelAndView applicationSubmittedModelAndView() {
    ModelAndView modelAndView = new ModelAndView("fcs/application/submissionConfirmation");
    modelAndView.addObject("pageTitle", PAGE_TITLE);

    // TODO FCS-36: replace CASE_REF with the case reference generated
    modelAndView.addObject("caseReference", "CASE_REF");
    modelAndView.addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
    return modelAndView;
  }
}

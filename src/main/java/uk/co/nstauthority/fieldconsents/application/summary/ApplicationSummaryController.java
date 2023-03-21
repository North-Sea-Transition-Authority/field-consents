package uk.co.nstauthority.fieldconsents.application.summary;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;

@Controller
@RequestMapping("applications/{applicationId}/summary")
public class ApplicationSummaryController {

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  ApplicationSummaryController(ApplicationVersionService applicationVersionService,
                               ApplicationSummaryService applicationSummaryService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
  }

  @GetMapping
  public ModelAndView getSummary(@PathVariable Integer applicationId) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var summarySections = applicationSummaryService.getSummarySections(applicationVersion);

    var wideSummaryDisplay =
        ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    return new ModelAndView("fcs/application/applicationSummary")
        .addObject("pageTitle", "Check your answers before submitting")
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay);
  }
}

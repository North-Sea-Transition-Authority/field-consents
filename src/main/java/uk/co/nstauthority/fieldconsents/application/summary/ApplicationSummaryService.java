package uk.co.nstauthority.fieldconsents.application.summary;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class ApplicationSummaryService {

  private final List<SummarySectionService<ApplicationVersion>> summarySectionServices;
  private final ApplicationVersionService applicationVersionService;

  @Autowired
  ApplicationSummaryService(List<SummarySectionService<ApplicationVersion>> summarySectionServices,
                            ApplicationVersionService applicationVersionService) {
    this.summarySectionServices = summarySectionServices;
    this.applicationVersionService = applicationVersionService;
  }

  public List<SummarySection> getSummarySections(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    return summarySectionServices.stream()
        .map(summarySectionService -> summarySectionService.getSummarySection(applicationVersion, user))
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(SummarySection::displayOrder))
        .toList();
  }

  public ModelAndView getApplicationSummaryModelAndView(ApplicationVersion applicationVersion, String viewName,
                                                        String pageTitle, ServiceUserDetail user) {

    var summarySections = getSummarySections(applicationVersion, user);
    var wideSummaryDisplay = WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    return new ModelAndView(viewName)
        .addObject("pageTitle", pageTitle)
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay);
  }

  public ModelAndView addSummarySectionsToModelAndView(
      ApplicationVersion applicationVersion,
      ModelAndView modelAndView,
      ServiceUserDetail user
  ) {
    var summarySections = getSummarySections(applicationVersion, user);
    var wideSummaryDisplay = WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    modelAndView
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay);

    return modelAndView;
  }

  public void addSummarySectionsAndVersionOptionsToModelAndView(
      ApplicationVersion selectedApplicationVersion,
      ModelAndView modelAndView,
      ServiceUserDetail user
  ) {
    addSummarySectionsToModelAndView(selectedApplicationVersion, modelAndView, user);

    var applicationVersionViews = applicationVersionService
        .getAllNonDeletedApplicationVersionsByApplicationId(selectedApplicationVersion.getApplication().getId())
        .stream()
        .sorted(Comparator.comparing(ApplicationVersion::getId).reversed())
        .map(ApplicationVersionView::from)
        .toList();

    modelAndView
        .addObject("selectedApplicationVersionView", ApplicationVersionView.from(selectedApplicationVersion))
        .addObject("applicationVersionViews", applicationVersionViews);
  }
}

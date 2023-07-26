package uk.co.nstauthority.fieldconsents.application.summary;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class ApplicationSummaryService {

  private final List<SummarySectionService<ApplicationVersion>> summarySectionServices;

  @Autowired
  ApplicationSummaryService(List<SummarySectionService<ApplicationVersion>> summarySectionServices) {
    this.summarySectionServices = summarySectionServices;
  }

  public List<SummarySection> getSummarySections(ApplicationVersion applicationVersion) {
    return summarySectionServices.stream()
        .map(summarySectionService -> summarySectionService.getSummarySection(applicationVersion))
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(SummarySection::displayOrder))
        .toList();
  }

  public ModelAndView getApplicationSummaryModelAndView(ApplicationVersion applicationVersion, String viewName,
                                                        String pageTitle, String backLinkUrl) {

    var summarySections = getSummarySections(applicationVersion);
    var wideSummaryDisplay = WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    return new ModelAndView(viewName)
        .addObject("pageTitle", pageTitle)
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay)
        .addObject("backLinkUrl", backLinkUrl);
  }

  public void addSummarySectionsToModelAndView(ApplicationVersion applicationVersion, ModelAndView modelAndView) {
    var summarySections = getSummarySections(applicationVersion);
    var wideSummaryDisplay = WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    modelAndView
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay);
  }

}

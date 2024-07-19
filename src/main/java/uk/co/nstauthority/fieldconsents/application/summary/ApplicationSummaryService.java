package uk.co.nstauthority.fieldconsents.application.summary;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

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
      ApplicationVersion applicationVersion,
      ModelAndView modelAndView,
      ServiceUserDetail user
  ) {
    addSummarySectionsToModelAndView(applicationVersion, modelAndView, user);

    var viewableApplicationVersions = applicationVersionService
        .getAllNonDeletedApplicationVersionsByApplicationId(applicationVersion.getApplication().getId());

    // if more than one application version available allow user to view previous versions
    if (viewableApplicationVersions.size() > 1) {
      modelAndView
          .addObject("currentVersionNumber", applicationVersion.getVersion())
          .addObject("availableVersions", getApplicationVersionOptions(viewableApplicationVersions));
    }
  }

  private Map<Integer, String> getApplicationVersionOptions(List<ApplicationVersion> applicationVersions) {
    return applicationVersions.stream()
        .sorted(Comparator.comparing(ApplicationVersion::getVersion).reversed())
        .collect(StreamUtils.toLinkedHashMap(
            ApplicationVersion::getVersion,
            this::getApplicationVersionDisplayText
        ));
  }

  String getApplicationVersionDisplayText(ApplicationVersion applicationVersion) {
    return "%s: %s".formatted(
        "Version " + applicationVersion.getVersion(),
        applicationVersion.getSubmittedDateTime() != null
            ? DateUtils.format(applicationVersion.getSubmittedDateTime(), DateUtils.SHORT_DATE)
            : applicationVersion.getStatus().getDisplayName()
    );
  }
}

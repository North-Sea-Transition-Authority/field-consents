package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import java.util.Comparator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationVersionView;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class CaseProcessingControllerHelperService {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;

  CaseProcessingControllerHelperService(
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
  }

  public ApplicationVersion getApplicationVersionForApplication(Application application, Integer requestedApplicationVersionId) {
    var requestedApplicationVersion = applicationVersionService.getApplicationVersionById(requestedApplicationVersionId);

    if (requestedApplicationVersion.getStatus() == ApplicationVersionStatus.DELETED) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    if (!requestedApplicationVersion.getApplication().getId().equals(application.getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    return requestedApplicationVersion;
  }

  public void addSummarySectionsAndVersionOptionsToModelAndView(
      ApplicationVersion selectedApplicationVersion,
      ModelAndView modelAndView,
      ServiceUserDetail user
  ) {
    applicationSummaryService.addSummarySectionsToModelAndView(selectedApplicationVersion, modelAndView, user);

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

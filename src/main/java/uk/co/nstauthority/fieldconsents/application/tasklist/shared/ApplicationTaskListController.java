package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ProductionConsentCheckResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.delete.DeleteApplicationController;
import uk.co.nstauthority.fieldconsents.application.licenceexpiry.LicenceExpiryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanEditApplication;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Controller
@RequestMapping("applications/{applicationId}/task-list")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@UserCanEditApplication
public class ApplicationTaskListController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationTaskListService applicationTaskListService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final FieldConsentsAccessService fieldConsentsAccessService;
  private final ConsentService consentService;
  private final LicenceExpiryService licenceExpiryService;

  ApplicationTaskListController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationTaskListService applicationTaskListService,
      ApplicationContextService applicationContextService,
      ApplicationUpdateService applicationUpdateService,
      ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
      FieldConsentsAccessService fieldConsentsAccessService,
      ConsentService consentService,
      LicenceExpiryService licenceExpiryService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationTaskListService = applicationTaskListService;
    this.applicationContextService = applicationContextService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.fieldConsentsAccessService = fieldConsentsAccessService;
    this.consentService = consentService;
    this.licenceExpiryService = licenceExpiryService;
  }

  @GetMapping
  public ModelAndView getTaskList(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var sections = applicationTaskListService.getAllSections(applicationVersion);
    var applicationType = applicationVersion.getApplication().getType();
    var applicationContext = applicationContextService.getApplicationContext(applicationVersion);
    var applicationReference = applicationService.getApplicationReference(applicationVersion);
    var hasPermissionToDeleteApplication =
        fieldConsentsAccessService.userHasAnyIndustryRole(user, applicationVersion, Set.of(Role.CREATOR));

    var modelAndView = new ModelAndView("fcs/application/applicationTaskList")
        .addObject("pageTitle", applicationType.getDisplayName() + " application")
        .addObject("taskListSections", sections)
        .addObject("applicationContext", applicationContext)
        .addObject("applicationReference", applicationReference)
        .addObject("hasPermissionToDeleteApplication", hasPermissionToDeleteApplication)
        .addObject("deleteApplicationUrl", ReverseRouter.route(on(DeleteApplicationController.class)
            .getDeleteApplication(applicationId, user)));

    if (consentService.shouldCheckProductionConsentExists(applicationVersion)) {
      var productionConsentCheckResult = consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion);
      if (productionConsentCheckResult == ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT) {
        modelAndView.addObject("warning", productionConsentCheckResult.getWarning());
      }
    }

    if (applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      modelAndView.addObject("applicationUpdateRequestView",
          applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion));
    }

    modelAndView.addObject("expiringLicences",
        licenceExpiryService.getLicencesExpiringDuringConsentPeriod(applicationVersion));

    return modelAndView;
  }
}

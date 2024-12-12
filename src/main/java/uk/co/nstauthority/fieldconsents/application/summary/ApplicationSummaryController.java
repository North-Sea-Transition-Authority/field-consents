package uk.co.nstauthority.fieldconsents.application.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus.IN_PROGRESS;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationOrRegulatorRole;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Controller
@RequestMapping("applications/{applicationId}")
@HasApplicationOrRegulatorRole(
    regulatorRoles = {
        // should match RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES
        Role.CASE_OFFICER,
        Role.CASE_MANAGER,
        Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
        Role.TECHNICAL_REVIEWER,
        Role.VIEWER
    },
    consulteeRoles = {
        // should match RoleGroup.CONSULTEE_VIEW_CASE_PROCESSING_ROLES
        Role.ALLOCATOR,
        Role.RESPONDER
    },
    industryRoles = {
        // should match RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES
        Role.CREATOR,
        Role.EDITOR,
        Role.SUBMITTER,
        Role.FINANCE_ADMINISTRATOR,
        Role.VIEWER,
        Role.CONSENT_RECIPIENT
    }
)
public class ApplicationSummaryController {

  private final ApplicationVersionService applicationVersionService;
  private final FieldConsentsAccessService fieldConsentsAccessService;

  ApplicationSummaryController(
      ApplicationVersionService applicationVersionService,
      FieldConsentsAccessService fieldConsentsAccessService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.fieldConsentsAccessService = fieldConsentsAccessService;
  }

  @GetMapping("summary")
  public ModelAndView getApplicationSummary(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var isFirstVersion = applicationVersion.isFirstVersion();

    if (fieldConsentsAccessService
        .userHasAnyRegulatorRole(user, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES)) {
      return ReverseRouter.redirect(on(ApplicationCaseProcessingController.class)
          .caseProcessing(applicationId, null, null, null));
    }

    if (fieldConsentsAccessService
        .userHasAnyConsulteeRole(user, applicationVersion, RoleGroup.CONSULTEE_VIEW_CASE_PROCESSING_ROLES)) {
      return ReverseRouter.redirect(on(ConsulteeCaseProcessingController.class)
          .caseProcessing(applicationId, null, null, null));
    }

    if (fieldConsentsAccessService
        .userHasAnyIndustryRole(user, applicationVersion, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES)) {
      var applicationStatus = applicationVersion.getStatus();
      if (isFirstVersion && IN_PROGRESS.equals(applicationStatus)) {
        return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
      }
      return ReverseRouter.redirect(on(IndustryCaseProcessingController.class)
          .getIndustryCaseProcessing(applicationId, null, null, null));
    }

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported user type");
  }
}

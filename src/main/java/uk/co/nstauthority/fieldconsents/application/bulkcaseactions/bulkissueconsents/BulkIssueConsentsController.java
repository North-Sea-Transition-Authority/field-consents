package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.servlet.http.HttpSession;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("bulk-case-actions/issue-consents")
@HasPermission(permissions = RolePermission.AUTHORISE_FCS_CONSENTS)
public class BulkIssueConsentsController {

  public static final String BULK_ISSUE_CONSENTS = "Bulk issue consents";

  private final ApplicationVersionService applicationVersionService;
  private final BulkIssueConsentsFormValidator validator;
  private final BulkIssueConsentsService bulkIssueConsentsService;
  private final BulkCaseActionService bulkCaseActionService;

  BulkIssueConsentsController(
      ApplicationVersionService applicationVersionService,
      BulkIssueConsentsFormValidator validator,
      BulkIssueConsentsService bulkIssueConsentsService,
      BulkCaseActionService bulkCaseActionService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.validator = validator;
    this.bulkIssueConsentsService = bulkIssueConsentsService;
    this.bulkCaseActionService = bulkCaseActionService;
  }

  @GetMapping
  ModelAndView viewSelectedApplications(HttpSession session, ServiceUserDetail user) {
    var sessionContext = BulkIssueConsentsSessionContext.fromSession(session);
    return getModelAndView(sessionContext, user, BulkIssueConsentsForm.empty());
  }

  @PostMapping
  ModelAndView bulkIssueConsents(
      HttpSession session,
      @ModelAttribute("form") BulkIssueConsentsForm form,
      BindingResult bindingResult,
      ServiceUserDetail user
  ) {
    var sessionContext = BulkIssueConsentsSessionContext.fromSession(session);

    validator.validate(form, bindingResult);
    if (bindingResult.hasErrors()) {
      return getModelAndView(sessionContext, user, form);
    }

    var applicationIds = form.selectedApplicationIds().stream().map(Integer::parseInt).collect(Collectors.toSet());
    var applicationVersions = applicationVersionService.getLatestApplicationVersions(applicationIds);

    bulkIssueConsentsService.queueApplicationsForIssue(applicationVersions, user);
    sessionContext.clearSelectedApplications();

    return ReverseRouter.redirect(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null));
  }

  private ModelAndView getModelAndView(
      BulkIssueConsentsSessionContext sessionContext,
      ServiceUserDetail user,
      BulkIssueConsentsForm form
  ) {
    var selectedApplicationIds = sessionContext.getSelectedApplicationsForm().getSelectedApplicationIds();
    var applicationDataItemViews = bulkCaseActionService.getSelectedApplicationDataItemViews(selectedApplicationIds, user);

    return new ModelAndView("fcs/application/bulk-case-actions/issueConsents")
        .addObject("form", form)
        .addObject("pageTitle", BULK_ISSUE_CONSENTS)
        .addObject("backLinkUrl", ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null)))
        .addObject("applicationDataItemViews", applicationDataItemViews)
        .addObject("captionHeadingFunction", (Function<ApplicationDataItemView, String>) this::captionHeadingFunction);
  }

  String captionHeadingFunction(ApplicationDataItemView applicationDataItem) {
    return "%s. %s - %s".formatted(
        Boolean.TRUE.equals(applicationDataItem.aceFlag())
            ? AceFlagStatus.ACE.getDisplayName()
            : AceFlagStatus.NON_ACE.getDisplayName(),
        applicationDataItem.operator(),
        applicationDataItem.asset()
    );
  }

}

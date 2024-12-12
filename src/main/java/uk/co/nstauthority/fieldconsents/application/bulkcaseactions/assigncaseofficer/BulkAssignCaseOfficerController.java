package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.role.HasRegulatorRole;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Controller
@RequestMapping("bulk-case-actions/assign-case-officer")
@HasRegulatorRole(Role.CASE_MANAGER)
public class BulkAssignCaseOfficerController {

  public static final String ASSIGN_CASE_OFFICER = "Assign case officer";

  private final ApplicationVersionService applicationVersionService;
  private final BulkAssignCaseOfficerFormValidator validator;
  private final BulkAssignCaseOfficerService bulkAssignCaseOfficerService;
  private final BulkCaseActionService bulkCaseActionService;
  private final EnergyPortalUserService energyPortalUserService;

  BulkAssignCaseOfficerController(
      ApplicationVersionService applicationVersionService,
      BulkAssignCaseOfficerFormValidator validator,
      BulkAssignCaseOfficerService bulkAssignCaseOfficerService,
      BulkCaseActionService bulkCaseActionService,
      EnergyPortalUserService energyPortalUserService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.validator = validator;
    this.bulkAssignCaseOfficerService = bulkAssignCaseOfficerService;
    this.bulkCaseActionService = bulkCaseActionService;
    this.energyPortalUserService = energyPortalUserService;
  }

  @GetMapping
  ModelAndView assignCaseOfficer(HttpSession session, ServiceUserDetail user) {
    var sessionContext = BulkAssignCaseOfficerSessionContext.fromSession(session);
    return getModelAndView(sessionContext, user, BulkAssignCaseOfficerForm.empty());
  }

  @PostMapping
  ModelAndView assignCaseOfficer(
      HttpSession session,
      RedirectAttributes redirectAttributes,
      @ModelAttribute("form") BulkAssignCaseOfficerForm form,
      BindingResult bindingResult,
      ServiceUserDetail user
  ) {
    var sessionContext = BulkAssignCaseOfficerSessionContext.fromSession(session);

    validator.validate(form, bindingResult);
    if (bindingResult.hasErrors()) {
      return getModelAndView(sessionContext, user, form);
    }

    var caseOfficer = energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(form.caseOfficerWuaId()));
    var applicationIds = form.selectedApplicationIds().stream().map(Integer::parseInt).collect(Collectors.toSet());
    var applicationVersions = applicationVersionService.getLatestApplicationVersions(applicationIds);

    bulkAssignCaseOfficerService.assignCaseOfficer(applicationVersions, ServiceUserDetail.from(caseOfficer), user);

    sessionContext.clearSelectedApplications();

    var bannerMessage = bulkAssignCaseOfficerService.getNotificationBannerSuccessMessage(applicationIds.size(),
        caseOfficer);
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, bannerMessage);

    return ReverseRouter.redirect(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null));
  }

  private ModelAndView getModelAndView(
      BulkAssignCaseOfficerSessionContext sessionContext,
      ServiceUserDetail user,
      BulkAssignCaseOfficerForm form
  ) {
    var selectedApplicationIds = sessionContext.getSelectedApplicationsForm().getSelectedApplicationIds();
    var applicationDataItemViews = bulkCaseActionService.getSelectedApplicationDataItemViews(selectedApplicationIds,
        user);

    var caseOfficerOptions = bulkAssignCaseOfficerService.getAvailableCaseOfficers()
        .stream()
        .collect(StreamUtils.toLinkedHashMap(
            energyPortalUserDto -> energyPortalUserDto.webUserAccountId().toString(),
            EnergyPortalUserDto::displayName
        ));

    return new ModelAndView("fcs/application/bulk-case-actions/assignCaseOfficer")
        .addObject("form", form)
        .addObject("pageTitle", ASSIGN_CASE_OFFICER)
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))
        )
        .addObject("applicationDataItemViews", applicationDataItemViews)
        .addObject("captionHeadingFunction", (Function<ApplicationDataItemView, String>) this::captionHeadingFunction)
        .addObject("caseOfficerOptions", caseOfficerOptions);
  }

  private String captionHeadingFunction(ApplicationDataItemView applicationDataItem) {
    if (applicationDataItem.caseOfficer().isEmpty()) {
      return "No case officer currently assigned";
    }

    return "Current case officer: %s".formatted(applicationDataItem.caseOfficer());
  }

}

package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm.APPROVED_FOR_ISSUE_FILTER_OPTION;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.ALL_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.ALL_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.ALL_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_CAM_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.UNASSIGNED_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.UNASSIGNED_CONSULTATIONS;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.authorisation.role.HasAnyRegulatorRole;
import uk.co.nstauthority.fieldconsents.authorisation.role.HasConsulteeRole;
import uk.co.nstauthority.fieldconsents.authorisation.role.HasRegulatorRole;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Controller
// the ordering of the mappings is important here otherwise the top navigation always highlights the work area
// even if another page is on display
@RequestMapping({"/work-area", "/"})
@SessionAttributes({"workAreaFilter"})
@AccessibleByServiceUsers
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";
  public static final String WORK_AREA_ITEMS = "workAreaItems";
  private static final String IS_WORK_AREA_WITH_TABS = "isWorkAreaWithTabs";

  private final WorkAreaService workAreaService;
  private final WorkAreaFilterFormService workAreaFormService;
  private final WorkAreaFilterService workAreaFilterService;
  private final ApplicationDataFilterFormService applicationDataFilterFormService;
  private final CaseAssignmentService caseAssignmentService;
  private final TechnicalReviewAssignmentService technicalReviewAssignmentService;
  private final TeamQueryService teamQueryService;

  WorkAreaController(
      WorkAreaService workAreaService,
      WorkAreaFilterFormService workAreaFormService,
      WorkAreaFilterService workAreaFilterService,
      ApplicationDataFilterFormService applicationDataFilterFormService,
      CaseAssignmentService caseAssignmentService,
      TechnicalReviewAssignmentService technicalReviewAssignmentService,
      TeamQueryService teamQueryService
  ) {
    this.workAreaService = workAreaService;
    this.workAreaFormService = workAreaFormService;
    this.workAreaFilterService = workAreaFilterService;
    this.applicationDataFilterFormService = applicationDataFilterFormService;
    this.caseAssignmentService = caseAssignmentService;
    this.technicalReviewAssignmentService = technicalReviewAssignmentService;
    this.teamQueryService = teamQueryService;
  }

  @GetMapping
  public ModelAndView getWorkArea(@ModelAttribute("workAreaFilter") WorkAreaFilter filter, ServiceUserDetail user) {
    var teamRoles = teamQueryService.getTeamRoles(user);
    var teamTypes = teamRoles.stream().map(teamRole -> teamRole.getTeam().getTeamType()).collect(Collectors.toSet());
    var roles = teamRoles.stream().map(TeamRole::getRole).collect(Collectors.toSet());

    if (teamTypes.contains(TeamType.REGULATOR)) {
      if (roles.contains(Role.CASE_OFFICER)) {
        return renderRegulatorWorkAreaOnTab(filter, user, MY_APPLICATIONS);
      }

      if (roles.contains(Role.CASE_MANAGER)) {
        return renderRegulatorWorkAreaOnTab(filter, user, ALL_APPLICATIONS);
      }

      if (roles.contains(Role.TECHNICAL_REVIEWER)) {
        return renderRegulatorWorkAreaOnTab(filter, user, MY_TECHNICAL_REVIEWS);
      }

      if (roles.contains(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)) {
        return renderRegulatorWorkAreaOnTab(filter, user, MY_CAM_APPLICATIONS);
      }
    }

    if (teamTypes.contains(TeamType.CONSULTEE)) {
      if (roles.contains(Role.ALLOCATOR)) {
        return renderConsulteeWorkAreaOnTab(filter, user, UNASSIGNED_CONSULTATIONS);
      }

      if (roles.contains(Role.RESPONDER)) {
        return renderConsulteeWorkAreaOnTab(filter, user, MY_CONSULTATIONS);
      }
    }

    return getWorkAreaModelAndView(filter, user)
        .addObject(IS_WORK_AREA_WITH_TABS, false)
        .addObject(WORK_AREA_ITEMS, workAreaService.getIndustryWorkAreaItems(filter, user));
  }

  @GetMapping("case-officer-my-applications")
  @HasRegulatorRole(Role.CASE_OFFICER)
  public ModelAndView getWorkAreaCaseOfficerMyApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                           ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, MY_APPLICATIONS);
  }

  @PostMapping("case-officer-my-applications")
  @HasRegulatorRole(Role.CASE_OFFICER)
  public ModelAndView postWorkAreaCaseOfficerMyApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                            ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaCaseOfficerMyApplications(filter, user));
  }

  @GetMapping("my-technical-reviews")
  @HasRegulatorRole(Role.TECHNICAL_REVIEWER)
  public ModelAndView getWorkAreaMyTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                    ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, MY_TECHNICAL_REVIEWS);
  }

  @PostMapping("my-technical-reviews")
  @HasRegulatorRole(Role.TECHNICAL_REVIEWER)
  public ModelAndView postWorkAreaMyTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                     ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaMyTechnicalReviews(filter, user));
  }

  @GetMapping("case-officer-unassigned")
  @HasAnyRegulatorRole({Role.CASE_OFFICER, Role.CASE_MANAGER})
  public ModelAndView getWorkAreaCaseOfficerUnassignedApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                                   ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, UNASSIGNED_APPLICATIONS);
  }

  @PostMapping("case-officer-unassigned")
  @HasAnyRegulatorRole({Role.CASE_OFFICER, Role.CASE_MANAGER})
  public ModelAndView postWorkAreaCaseOfficerUnassignedApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                                    ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaCaseOfficerUnassignedApplications(filter, user));
  }

  @GetMapping("all-technical-reviews")
  @HasRegulatorRole(Role.TECHNICAL_REVIEWER)
  public ModelAndView getWorkAreaAllTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                     ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, ALL_TECHNICAL_REVIEWS);
  }

  @PostMapping("all-technical-reviews")
  @HasRegulatorRole(Role.TECHNICAL_REVIEWER)
  public ModelAndView postWorkAreaAllTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                      ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaAllTechnicalReviews(filter, user));
  }

  @GetMapping("regulator-all-applications")
  @HasAnyRegulatorRole({Role.CASE_MANAGER, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER})
  public ModelAndView getWorkAreaRegulatorAllApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                          ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, ALL_APPLICATIONS);
  }

  @PostMapping("regulator-all-applications")
  @HasAnyRegulatorRole({Role.CASE_MANAGER, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER})
  public ModelAndView postWorkAreaRegulatorAllApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                           ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaRegulatorAllApplications(filter, user));
  }

  @GetMapping("all-consultations")
  @HasConsulteeRole(Role.ALLOCATOR)
  public ModelAndView getWorkAreaAllConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                  ServiceUserDetail user) {
    return renderConsulteeWorkAreaOnTab(filter, user, ALL_CONSULTATIONS);
  }

  @PostMapping("all-consultations")
  @HasConsulteeRole(Role.ALLOCATOR)
  public ModelAndView postWorkAreaAllConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                   ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaAllConsultations(filter, user));
  }

  @GetMapping("unassigned-consultations")
  @HasConsulteeRole(Role.ALLOCATOR)
  public ModelAndView getWorkAreaUnassignedConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                         ServiceUserDetail user) {
    return renderConsulteeWorkAreaOnTab(filter, user, UNASSIGNED_CONSULTATIONS);
  }

  @PostMapping("unassigned-consultations")
  @HasConsulteeRole(Role.ALLOCATOR)
  public ModelAndView postWorkAreaUnassignedConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                          ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaUnassignedConsultations(filter, user));
  }

  @GetMapping("my-consultations")
  @HasConsulteeRole(Role.RESPONDER)
  public ModelAndView getWorkAreaMyConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                 ServiceUserDetail user) {
    return renderConsulteeWorkAreaOnTab(filter, user, MY_CONSULTATIONS);
  }

  @PostMapping("my-consultations")
  @HasConsulteeRole(Role.RESPONDER)
  public ModelAndView postWorkAreaMyConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaMyConsultations(null, null));
  }

  @GetMapping("cam-my-applications")
  @HasRegulatorRole(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
  public ModelAndView getWorkAreaCamMyApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                   ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, MY_CAM_APPLICATIONS);
  }

  @PostMapping("cam-my-applications")
  @HasRegulatorRole(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
  public ModelAndView postWorkAreaCamMyApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                    ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaCamMyApplications(filter, user));
  }

  private ModelAndView renderRegulatorWorkAreaOnTab(WorkAreaFilter filter, ServiceUserDetail user, WorkAreaTab workAreaTab) {
    var caseOfficerFilterEnabled = teamQueryService.getTeamRoles(user)
        .stream()
        .map(TeamRole::getRole)
        .anyMatch(role -> role == Role.CASE_MANAGER
            || role == Role.TECHNICAL_REVIEWER
            || role == Role.CONSENTS_AND_AUTHORISATIONS_MANAGER
        );

    var caseOfficersById = caseOfficerFilterEnabled ? convertUsersToMap(caseAssignmentService.getCurrentCaseOfficers()) : null;
    var technicalReviewersById = convertUsersToMap(technicalReviewAssignmentService.getCurrentTechnicalReviewers());
    return getWorkAreaModelAndView(filter, user)
        .addObject("selectedTab", workAreaTab.getValue())
        .addObject(WORK_AREA_ITEMS, workAreaService.getRegulatorWorkAreaItems(filter, user, workAreaTab))
        .addObject(IS_WORK_AREA_WITH_TABS, true)
        .addObject("caseOfficersAssigned", caseOfficersById)
        .addObject("technicalReviewersAssigned", technicalReviewersById)
        .addObject("approvedForIssue",
            Map.of(APPROVED_FOR_ISSUE_FILTER_OPTION, Boolean.TRUE.equals(filter.getApprovedForIssue())));
  }

  private ModelAndView renderConsulteeWorkAreaOnTab(WorkAreaFilter filter,
                                                    ServiceUserDetail user,
                                                    WorkAreaTab workAreaTab) {
    return getWorkAreaModelAndView(filter, user)
        .addObject("selectedTab", workAreaTab.getValue())
        .addObject(WORK_AREA_ITEMS, workAreaService.getConsulteeWorkAreaItems(filter, user, workAreaTab))
        .addObject(IS_WORK_AREA_WITH_TABS, true);
  }

  private ModelAndView getWorkAreaModelAndView(WorkAreaFilter filter, ServiceUserDetail user) {
    var appStatuses = ApplicationVersionStatus.getWorkAreaOptions();
    var appTypes = ApplicationType.getDisplayableOptions();
    var durationTypes = ConsentLengthType.getConsentLengthOptions();
    var form = workAreaFormService.getFromFilter(filter);
    var prefilledOperator = applicationDataFilterFormService.getPrefilledOrganisation(form.getOperatorId());
    var prefilledAsset = applicationDataFilterFormService.getPrefilledAsset(form.getAssetKey());
    var geographicAreas = GeographicArea.getDisplayableOptions();
    var assetTypesWithShore = AssetTypeWithShore.getDisplayableOptions();

    return new ModelAndView("fcs/workarea/workArea")
        .addObject("clearFiltersUrl",
            ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilter(null, null)))
        .addObject("appStatuses", appStatuses)
        .addObject("appTypes", appTypes)
        .addObject("durationTypes", durationTypes)
        .addObject("prefilledOperator", prefilledOperator)
        .addObject("operatorSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForViewer(null, null)))
        .addObject("prefilledAsset", prefilledAsset)
        .addObject("assetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser(null, null)))
        .addObject("geographicAreas", geographicAreas)
        .addObject("assetTypesWithShore", assetTypesWithShore)
        .addObject("form", form)
        .addObject("pageTitle", WORK_AREA_TITLE)
        .addObject("workAreaTabs", workAreaService.getTabsAvailableToUser(user));
  }

  private Map<String, String> convertUsersToMap(List<EnergyPortalUserDto> energyPortalUserDtos) {
    return energyPortalUserDtos.stream()
        .collect(StreamUtils.toLinkedHashMap(
            energyPortalUserDto -> energyPortalUserDto.webUserAccountId().toString(),
            EnergyPortalUserDto::displayName));
  }

  @PostMapping
  ModelAndView filterWorkArea(@ModelAttribute("form") WorkAreaFilterForm form,
                              @ModelAttribute("workAreaFilter") WorkAreaFilter filter) {
    filter.update(form);
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  @GetMapping("/clear-filters")
  public ModelAndView clearWorkAreaFilter(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                          SessionStatus sessionStatus) {
    sessionStatus.setComplete();
    filter.clearSession();
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  @ModelAttribute("workAreaFilter")
  private WorkAreaFilter getDefaultFilter(ServiceUserDetail user) {
    return workAreaFilterService.getDefaultFilter(user);
  }
}

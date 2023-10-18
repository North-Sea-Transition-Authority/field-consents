package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.ALL_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.ALL_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.ALL_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.UNASSIGNED_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.UNASSIGNED_CONSULTATIONS;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Controller
// the ordering of the mappings is important here otherwise the top navigation always highlights the work area
// even if another page is on display
@RequestMapping({"/work-area", "/"})
@SessionAttributes({"workAreaFilter"})
@AccessibleByServiceUsers
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";
  private static final String IS_WORK_AREA_WITH_TABS = "isWorkAreaWithTabs";
  private static final String WORK_AREA_ITEMS = "workAreaItems";

  private final WorkAreaService workAreaService;

  private final WorkAreaFilterFormService workAreaFormService;

  private final WorkAreaFilterService workAreaFilterService;

  private final TeamService teamService;

  private final PermissionService permissionService;

  private final ApplicationDataFilterFormService applicationDataFilterFormService;

  private final CaseAssignmentService caseAssignmentService;

  public WorkAreaController(WorkAreaService workAreaService,
                            WorkAreaFilterFormService workAreaFormService,
                            WorkAreaFilterService workAreaFilterService,
                            TeamService teamService,
                            PermissionService permissionService,
                            ApplicationDataFilterFormService applicationDataFilterFormService,
                            CaseAssignmentService caseAssignmentService) {
    this.workAreaService = workAreaService;
    this.workAreaFormService = workAreaFormService;
    this.workAreaFilterService = workAreaFilterService;
    this.teamService = teamService;
    this.permissionService = permissionService;
    this.applicationDataFilterFormService = applicationDataFilterFormService;
    this.caseAssignmentService = caseAssignmentService;
  }

  @GetMapping
  public ModelAndView getWorkArea(@ModelAttribute("workAreaFilter") WorkAreaFilter filter, ServiceUserDetail user) {
    if (teamService.isRegulatorUser(user)) {
      if (permissionService.hasPermission(user, EnumSet.of(RolePermission.PROCESS_FCS_APPLICATIONS))) {
        return renderRegulatorWorkAreaOnTab(filter, user, MY_APPLICATIONS);
      } else if (permissionService.hasPermission(user, EnumSet.of(RolePermission.ASSIGN_FCS_APPLICATIONS))) {
        return renderRegulatorWorkAreaOnTab(filter, user, ALL_APPLICATIONS);
      } else if (permissionService.hasPermission(user, EnumSet.of(RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS))) {
        return renderRegulatorWorkAreaOnTab(filter, user, MY_TECHNICAL_REVIEWS);
      }
    }

    if (teamService.isConsulteeUser(user)) {
      if (permissionService.hasPermission(user, EnumSet.of(RolePermission.ALLOCATE_CONSULTATION))) {
        return renderConsulteeWorkAreaOnTab(filter, user, ALL_CONSULTATIONS);
      }
      if (permissionService.hasPermission(user, EnumSet.of(RolePermission.RESPOND_TO_CONSULTATION))) {
        return renderConsulteeWorkAreaOnTab(filter, user, MY_CONSULTATIONS);
      }
    }

    return getWorkAreaModelAndView(filter, user)
        .addObject(IS_WORK_AREA_WITH_TABS, false)
        .addObject(WORK_AREA_ITEMS, workAreaService.getIndustryWorkAreaItems(filter, user));
  }

  @GetMapping("case-officer-my-applications")
  @HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
  public ModelAndView getWorkAreaCaseOfficerMyApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                           ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, MY_APPLICATIONS);
  }

  @PostMapping("case-officer-my-applications")
  @HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
  public ModelAndView postWorkAreaCaseOfficerMyApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                            ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaCaseOfficerMyApplications(filter, user));
  }

  @GetMapping("my-technical-reviews")
  @HasPermission(permissions = RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS)
  public ModelAndView getWorkAreaMyTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                    ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, MY_TECHNICAL_REVIEWS);
  }

  @PostMapping("my-technical-reviews")
  @HasPermission(permissions = RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS)
  public ModelAndView postWorkAreaMyTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                     ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaMyTechnicalReviews(filter, user));
  }

  @GetMapping("case-officer-unassigned")
  @HasPermission(permissions = {RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.ASSIGN_FCS_APPLICATIONS})
  public ModelAndView getWorkAreaCaseOfficerUnassignedApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                                   ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, UNASSIGNED_APPLICATIONS);
  }

  @PostMapping("case-officer-unassigned")
  @HasPermission(permissions = {RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.ASSIGN_FCS_APPLICATIONS})
  public ModelAndView postWorkAreaCaseOfficerUnassignedApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                                    ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaCaseOfficerUnassignedApplications(filter, user));
  }

  @GetMapping("all-technical-reviews")
  @HasPermission(permissions = RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS)
  public ModelAndView getWorkAreaAllTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                     ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, ALL_TECHNICAL_REVIEWS);
  }

  @PostMapping("all-technical-reviews")
  @HasPermission(permissions = RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS)
  public ModelAndView postWorkAreaAllTechnicalReviews(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                      ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaAllTechnicalReviews(filter, user));
  }

  @GetMapping("regulator-all-applications")
  @HasPermission(permissions = RolePermission.ASSIGN_FCS_APPLICATIONS)
  public ModelAndView getWorkAreaRegulatorAllApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                          ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, ALL_APPLICATIONS);
  }

  @PostMapping("regulator-all-applications")
  @HasPermission(permissions = RolePermission.ASSIGN_FCS_APPLICATIONS)
  public ModelAndView postWorkAreaRegulatorAllApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                           ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaRegulatorAllApplications(filter, user));
  }

  @GetMapping("all-consultations")
  @HasPermission(permissions = RolePermission.ALLOCATE_CONSULTATION)
  public ModelAndView getWorkAreaAllConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                  ServiceUserDetail user) {
    return renderConsulteeWorkAreaOnTab(filter, user, ALL_CONSULTATIONS);
  }

  @PostMapping("all-consultations")
  @HasPermission(permissions = RolePermission.ALLOCATE_CONSULTATION)
  public ModelAndView postWorkAreaAllConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                   ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaAllConsultations(filter, user));
  }

  @GetMapping("unassigned-consultations")
  @HasPermission(permissions = RolePermission.ALLOCATE_CONSULTATION)
  public ModelAndView getWorkAreaUnassignedConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                         ServiceUserDetail user) {
    return renderConsulteeWorkAreaOnTab(filter, user, UNASSIGNED_CONSULTATIONS);
  }

  @PostMapping("unassigned-consultations")
  @HasPermission(permissions = RolePermission.ALLOCATE_CONSULTATION)
  public ModelAndView postWorkAreaUnassignedConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                          ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaUnassignedConsultations(filter, user));
  }

  @GetMapping("my-consultations")
  @HasPermission(permissions = RolePermission.RESPOND_TO_CONSULTATION)
  public ModelAndView getWorkAreaMyConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                 ServiceUserDetail user) {
    return renderConsulteeWorkAreaOnTab(filter, user, MY_CONSULTATIONS);
  }

  @PostMapping("my-consultations")
  @HasPermission(permissions = RolePermission.RESPOND_TO_CONSULTATION)
  public ModelAndView postWorkAreaMyConsultations(@ModelAttribute("workAreaFilter") WorkAreaFilter filter) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaMyConsultations(null, null));
  }

  private ModelAndView renderRegulatorWorkAreaOnTab(WorkAreaFilter filter,
                                                    ServiceUserDetail user,
                                                    WorkAreaTab workAreaTab) {
    var caseOfficersAssignedMap = convertUsersToMap(caseAssignmentService.getCurrentCaseOfficers());
    return getWorkAreaModelAndView(filter, user)
        .addObject("selectedTab", workAreaTab.getValue())
        .addObject(WORK_AREA_ITEMS, workAreaService.getRegulatorWorkAreaItems(filter, user, workAreaTab))
        .addObject(IS_WORK_AREA_WITH_TABS, true)
        .addObject("caseOfficersAssigned", caseOfficersAssignedMap);
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

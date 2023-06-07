package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.ALL_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.MY_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaTab.UNASSIGNED_APPLICATIONS;

import java.util.EnumSet;
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
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
// the ordering of the mappings is important here otherwise the top navigation always highlights the work area
// even if another page is on display
@RequestMapping({"/work-area", "/"})
@SessionAttributes({"workAreaFilter"})
@AccessibleByServiceUsers
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";

  private final WorkAreaService workAreaService;

  private final WorkAreaFormService workAreaFormService;

  private final WorkAreaFilterService workAreaFilterService;

  private final TeamService teamService;

  private final PermissionService permissionService;


  public WorkAreaController(WorkAreaService workAreaService,
                            WorkAreaFormService workAreaFormService,
                            WorkAreaFilterService workAreaFilterService,
                            TeamService teamService, PermissionService permissionService) {
    this.workAreaService = workAreaService;
    this.workAreaFormService = workAreaFormService;
    this.workAreaFilterService = workAreaFilterService;
    this.teamService = teamService;
    this.permissionService = permissionService;
  }

  @GetMapping
  public ModelAndView getWorkArea(@ModelAttribute("workAreaFilter") WorkAreaFilter filter, ServiceUserDetail user) {
    var isRegulatorUser = teamService.isRegulatorUser(user);

    if (isRegulatorUser) {
      if (permissionService.hasPermission(user, EnumSet.of(RolePermission.PROCESS_FCS_APPLICATIONS))) {
        return renderRegulatorWorkAreaOnTab(filter, user, MY_APPLICATIONS);
      } else if (permissionService.hasPermission(user, EnumSet.of(RolePermission.ASSIGN_FCS_APPLICATIONS))) {
        return renderRegulatorWorkAreaOnTab(filter, user, ALL_APPLICATIONS);
      }
    }

    return getWorkAreaModelAndView(filter, user)
        .addObject("isRegulatorUser", isRegulatorUser)
        .addObject("workAreaItems", workAreaService.getIndustryWorkAreaItems(filter, user));
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

  @GetMapping("case-officer-unassigned")
  @HasPermission(permissions = {RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.ASSIGN_FCS_APPLICATIONS})
  public ModelAndView getWorkAreaCaseOfficerUnassignedApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                                   ServiceUserDetail user) {
    return renderRegulatorWorkAreaOnTab(filter, user, UNASSIGNED_APPLICATIONS);
  }

  @PostMapping("case-officer-unassigned")
  @HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
  public ModelAndView postWorkAreaCaseOfficerUnassignedApplications(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                                                    ServiceUserDetail user) {
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkAreaCaseOfficerUnassignedApplications(filter, user));
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

  private ModelAndView renderRegulatorWorkAreaOnTab(WorkAreaFilter filter, ServiceUserDetail user,
                                                    WorkAreaTab workAreaTab) {
    return getWorkAreaModelAndView(filter, user)
        .addObject("selectedTab", workAreaTab.getValue())
        .addObject("workAreaItems", workAreaService.getRegulatorWorkAreaItems(filter, user, workAreaTab))
        .addObject("isRegulatorUser", true);
  }

  private ModelAndView getWorkAreaModelAndView(WorkAreaFilter filter, ServiceUserDetail user) {
    var appStatuses = ApplicationVersionStatus.getWorkAreaOptions();
    var appTypes = ApplicationType.getDisplayableOptions();
    var durationTypes = ConsentLengthType.getWorkAreaOptions();
    var form = workAreaFormService.getFromFilter(filter);
    var prefilledOperator = workAreaFormService.getPrefilledOrganisation(form.getOperatorId());
    var prefilledAsset = workAreaFormService.getPrefilledAsset(form.getAssetKey());
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
            ReverseRouter.route(on(OrganisationUnitRestController.class).getOrganisationUnitsForEditor(null, null)))
        .addObject("prefilledAsset", prefilledAsset)
        .addObject("assetSearchRestUrl",
            ReverseRouter.route(on(AssetRestController.class).searchAssetsForUser(null, null)))
        .addObject("geographicAreas", geographicAreas)
        .addObject("assetTypesWithShore", assetTypesWithShore)
        .addObject("form", form)
        .addObject("pageTitle", WORK_AREA_TITLE)
        .addObject("workAreaTabs", workAreaService.getTabsAvailableToUser(user));
  }

  @PostMapping
  ModelAndView filterWorkArea(@ModelAttribute("form") WorkAreaForm form,
                              @ModelAttribute("workAreaFilter") WorkAreaFilter filter) {
    filter.update(form);
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  @GetMapping("/clear-filters")
  public ModelAndView clearWorkAreaFilter(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                          SessionStatus sessionStatus) {
    sessionStatus.setComplete();
    filter.clearFilter();
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  @ModelAttribute("workAreaFilter")
  private WorkAreaFilter getDefaultFilter(ServiceUserDetail user) {
    return workAreaFilterService.getDefaultFilter(user);
  }
}

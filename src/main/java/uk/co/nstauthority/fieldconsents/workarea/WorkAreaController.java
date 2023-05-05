package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
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
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
// the ordering of the mappings is important here otherwise the top navigation always highlights the work area
// even if another page is on display
@RequestMapping({"/work-area", "/"})
@SessionAttributes({"workAreaFilter"})
@AccessibleByServiceUsers
@HasPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
public class WorkAreaController {

  private final WorkAreaService workAreaService;

  public static final String WORK_AREA_TITLE = "Work area";

  public WorkAreaController(WorkAreaService workAreaService) {
    this.workAreaService = workAreaService;
  }

  @GetMapping
  public ModelAndView getWorkArea(@ModelAttribute("workAreaFilter") WorkAreaFilter filter) {
    var workAreaItems = workAreaService.getWorkAreaItems(filter);
    var appStatuses = ApplicationVersionStatus.getWorkAreaOptions();
    var appTypes = ApplicationType.getDisplayableOptions();
    var form = WorkAreaForm.from(filter);

    return new ModelAndView("fcs/workarea/workArea")
        .addObject("workAreaItems", workAreaItems)
        .addObject("clearFiltersUrl",
            ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilter(null, null)))
        .addObject("appStatuses", appStatuses)
        .addObject("appTypes", appTypes)
        .addObject("form", form)
        .addObject("pageTitle", WORK_AREA_TITLE);
  }

  @PostMapping
  ModelAndView filterWorkArea(@ModelAttribute("form") WorkAreaForm form,
                              @ModelAttribute("workAreaFilter") WorkAreaFilter filter) {
    filter.update(form);
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null));
  }

  @GetMapping("/clear-filters")
  public ModelAndView clearWorkAreaFilter(@ModelAttribute("workAreaFilter") WorkAreaFilter filter,
                                          SessionStatus sessionStatus) {
    sessionStatus.setComplete();
    filter.clearFilter();
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null));
  }

  @ModelAttribute("workAreaFilter")
  private WorkAreaFilter getDefaultFilter() {
    var defaultFilter = new WorkAreaFilter();
    defaultFilter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    defaultFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return defaultFilter;
  }
}

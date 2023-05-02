package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/vent-report")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class VentReportController {

  private final ApplicationVersionService applicationVersionService;

  private final VentReportService ventReportService;

  private final VentReportPeriodService ventReportPeriodService;

  private final VentReportFormService ventReportFormService;

  private final ApplicationUnitService applicationUnitService;

  @Autowired
  VentReportController(ApplicationVersionService applicationVersionService,
                       VentReportService ventReportService,
                       VentReportPeriodService ventReportPeriodService,
                       VentReportFormService ventReportFormService,
                       ApplicationUnitService applicationUnitService) {
    this.applicationVersionService = applicationVersionService;
    this.ventReportService = ventReportService;
    this.ventReportPeriodService = ventReportPeriodService;
    this.ventReportFormService = ventReportFormService;
    this.applicationUnitService = applicationUnitService;
  }

  @GetMapping
  public ModelAndView getVentReportForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    // if there is no vent report period then redirect there
    if (!ventReportPeriodService.ventReportPeriodExists(applicationVersion)) {
      return ReverseRouter.redirect(on(VentReportPeriodController.class).getVentReportPeriodForm(applicationId));
    }

    VentReportForm ventReportForm =
        ventReportService.getVentReportForm(applicationVersion);

    var modelAndView = getVentReportModelAndView(applicationId, ventReportForm);
    modelAndView.addObject("form", ventReportForm);
    return modelAndView;
  }

  private ModelAndView getVentReportModelAndView(Integer applicationId, VentReportForm ventReportForm) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    String pageTitleYearText = Objects.equals(ventReportForm.getStartYear(), ventReportForm.getEndYear())
        ? ventReportForm.getStartYear() : ventReportForm.getStartYear() + "/" + ventReportForm.getEndYear();
    String pageTitle = "Vent report " + pageTitleYearText;

    ModelAndView modelAndView = new ModelAndView("fcs/vent/ventReportForm");
    modelAndView
        .addObject("pageTitle", pageTitle)
        .addObject("categoryUnit",
            applicationUnitService.getVentCategoryUnit(applicationVersion).getDisplayName())
        .addObject("periodUrl", ReverseRouter.route(on(VentReportPeriodController.class)
            .getVentReportPeriodForm(applicationId)))
        .addObject("submitUrl", ReverseRouter.route(on(VentReportController.class)
            .saveVentReportForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveVentReportForm(@PathVariable Integer applicationId,
                                         @ModelAttribute("form") VentReportForm form,
                                         BindingResult bindingResult) {

    bindingResult = ventReportFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getVentReportModelAndView(applicationId, form);
    }

    ventReportService.saveVentReport(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}

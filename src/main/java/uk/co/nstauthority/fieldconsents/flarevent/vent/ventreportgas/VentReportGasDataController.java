package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.LONG_MONTH_YEAR;

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
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataFormValidator;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/vent-report-gas-properties")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class VentReportGasDataController {

  private final VentReportGasDataService ventReportGasDataService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationUnitService applicationUnitService;

  private final FlareVentReportGasDataFormValidator flareVentReportGasDataFormValidator;

  private final VentReportPeriodService ventReportPeriodService;

  @Autowired
  public VentReportGasDataController(VentReportGasDataService ventReportGasDataService,
                                     ApplicationVersionService applicationVersionService,
                                     ApplicationUnitService applicationUnitService,
                                     FlareVentReportGasDataFormValidator flareVentReportGasDataFormValidator,
                                     VentReportPeriodService ventReportPeriodService) {
    this.ventReportGasDataService = ventReportGasDataService;
    this.applicationVersionService = applicationVersionService;
    this.applicationUnitService = applicationUnitService;
    this.flareVentReportGasDataFormValidator = flareVentReportGasDataFormValidator;
    this.ventReportPeriodService = ventReportPeriodService;
  }

  @GetMapping
  public ModelAndView getVentReportGasDataForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);

    FlareVentReportGasDataForm reportGasDataForm = ventReportGasDataService.getFlareVentReportGasDataForm(applicationVersion);

    var modelAndView = getVentReportGasDataModelAndView(applicationId);
    modelAndView.addObject("form", reportGasDataForm);
    return modelAndView;
  }

  private ModelAndView getVentReportGasDataModelAndView(Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var modelAndView = new ModelAndView("fcs/vent/ventReportGasDataForm");
    VentReportPeriod ventReportPeriod = ventReportPeriodService.getVentReportPeriodOrError(applicationVersion);

    modelAndView
        .addObject("reportPeriodStart",
            DateUtils.format(ventReportPeriod.getReportStartYearMonth(), LONG_MONTH_YEAR))
        .addObject("reportPeriodEnd",
            DateUtils.format(ventReportPeriod.getReportEndYearMonth(), LONG_MONTH_YEAR))
        .addObject("standardDensityUnit",
            applicationUnitService.getVentGasDensityUnit(applicationVersion).getDisplayName())
        .addObject("gasContentUnit",
            applicationUnitService.getVentGasContentUnit(applicationVersion).getDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(VentReportGasDataController.class)
            .saveVentReportGasDataForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId, null)));

    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveVentReportGasDataForm(@PathVariable Integer applicationId,
                                                @ModelAttribute("form") FlareVentReportGasDataForm form,
                                                BindingResult bindingResult) {

    flareVentReportGasDataFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getVentReportGasDataModelAndView(applicationId);
    }

    ventReportGasDataService.saveVentReportGasData(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
  }
}

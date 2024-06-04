package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

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
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/flare-report-gas-properties")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class FlareReportGasDataController {

  private final FlareReportGasDataService flareReportGasDataService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationUnitService applicationUnitService;

  private final FlareVentReportGasDataFormValidator flareVentReportGasDataFormValidator;

  private final FlareReportPeriodService flareReportPeriodService;

  @Autowired
  public FlareReportGasDataController(FlareReportGasDataService flareReportGasDataService,
                                      ApplicationVersionService applicationVersionService,
                                      ApplicationUnitService applicationUnitService,
                                      FlareVentReportGasDataFormValidator flareVentReportGasDataFormValidator,
                                      FlareReportPeriodService flareReportPeriodService) {
    this.flareReportGasDataService = flareReportGasDataService;
    this.applicationVersionService = applicationVersionService;
    this.applicationUnitService = applicationUnitService;
    this.flareVentReportGasDataFormValidator = flareVentReportGasDataFormValidator;
    this.flareReportPeriodService = flareReportPeriodService;
  }

  @GetMapping
  public ModelAndView getFlareReportGasDataForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);

    FlareVentReportGasDataForm reportGasDataForm = flareReportGasDataService.getFlareVentReportGasDataForm(applicationVersion);

    var modelAndView = getFlareReportGasDataModelAndView(applicationId);
    modelAndView.addObject("form", reportGasDataForm);
    return modelAndView;
  }

  private ModelAndView getFlareReportGasDataModelAndView(Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var modelAndView = new ModelAndView("fcs/flare/flareReportGasDataForm");
    FlareReportPeriod flareReportPeriod = flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion);

    modelAndView
        .addObject("reportPeriodStart",
            DateUtils.format(flareReportPeriod.getReportStartYearMonth(), LONG_MONTH_YEAR))
        .addObject("reportPeriodEnd",
            DateUtils.format(flareReportPeriod.getReportEndYearMonth(), LONG_MONTH_YEAR))
        .addObject("standardDensityUnit",
            applicationUnitService.getFlareGasDensityUnit(applicationVersion).getDisplayName())
        .addObject("gasContentUnit",
            applicationUnitService.getFlareGasContentUnit(applicationVersion).getDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(FlareReportGasDataController.class)
            .saveFlareReportGasDataForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId, null)));

    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveFlareReportGasDataForm(@PathVariable Integer applicationId,
                                                 @ModelAttribute("form") FlareVentReportGasDataForm form,
                                                 BindingResult bindingResult) {

    flareVentReportGasDataFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFlareReportGasDataModelAndView(applicationId);
    }

    flareReportGasDataService.saveFlareReportGasData(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
  }
}

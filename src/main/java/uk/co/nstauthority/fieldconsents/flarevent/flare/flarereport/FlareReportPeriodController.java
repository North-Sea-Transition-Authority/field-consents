package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodControllerHelperService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodFormValidator;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/flare-report/period")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class FlareReportPeriodController {

  private final ApplicationVersionService applicationVersionService;

  private final FlareReportPeriodService flareReportPeriodService;

  private final FlareVentReportPeriodFormValidator reportPeriodFormValidator;

  private final FlareVentReportPeriodControllerHelperService reportPeriodControllerHelperService;

  @Autowired
  FlareReportPeriodController(ApplicationVersionService applicationVersionService,
                              FlareReportPeriodService flareReportPeriodService,
                              FlareVentReportPeriodFormValidator reportPeriodFormValidator,
                              FlareVentReportPeriodControllerHelperService reportPeriodControllerHelperService) {
    this.applicationVersionService = applicationVersionService;
    this.flareReportPeriodService = flareReportPeriodService;
    this.reportPeriodFormValidator = reportPeriodFormValidator;
    this.reportPeriodControllerHelperService = reportPeriodControllerHelperService;
  }

  @GetMapping
  public ModelAndView getFlareReportPeriodForm(@PathVariable Integer applicationId) {

    FlareVentReportPeriodForm reportPeriodForm = flareReportPeriodService.getFlareReportPeriodForm(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId));

    var modelAndView = getFlareReportPeriodModelAndView(applicationId);

    modelAndView.addObject("form", reportPeriodForm);

    return modelAndView;
  }

  private ModelAndView getFlareReportPeriodModelAndView(Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var modelAndView = new ModelAndView("fcs/flare/flareReportPeriodForm");

    modelAndView
        .addObject("reportEndMonthsMap", DateUtils.monthsMap())
        .addObject("reportEndYearsMap",
            reportPeriodControllerHelperService.getReportEndYearsMap(applicationVersion))
        .addObject("submitUrl", ReverseRouter.route(on(FlareReportPeriodController.class)
            .saveFlareReportPeriodForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId, null)));

    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveFlareReportPeriodForm(@PathVariable Integer applicationId,
                                                @ModelAttribute("form") FlareVentReportPeriodForm form,
                                                BindingResult bindingResult) {

    reportPeriodFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFlareReportPeriodModelAndView(applicationId);
    }

    flareReportPeriodService.saveFlareReportPeriod(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(FlareReportController.class).getFlareReportForm(applicationId));
  }
}
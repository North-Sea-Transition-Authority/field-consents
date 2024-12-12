package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

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
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanEditApplication;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodControllerHelperService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodFormValidator;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/vent-report/period")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@UserCanEditApplication
public class VentReportPeriodController {

  private final ApplicationVersionService applicationVersionService;

  private final VentReportPeriodService ventReportPeriodService;

  private final FlareVentReportPeriodFormValidator reportPeriodFormValidator;

  private final FlareVentReportPeriodControllerHelperService reportPeriodControllerHelperService;

  @Autowired
  VentReportPeriodController(ApplicationVersionService applicationVersionService,
                             VentReportPeriodService ventReportPeriodService,
                             FlareVentReportPeriodFormValidator reportPeriodFormValidator,
                             FlareVentReportPeriodControllerHelperService reportPeriodControllerHelperService) {
    this.applicationVersionService = applicationVersionService;
    this.ventReportPeriodService = ventReportPeriodService;
    this.reportPeriodFormValidator = reportPeriodFormValidator;
    this.reportPeriodControllerHelperService = reportPeriodControllerHelperService;
  }

  @GetMapping
  public ModelAndView getVentReportPeriodForm(@PathVariable Integer applicationId) {

    FlareVentReportPeriodForm reportPeriodForm = ventReportPeriodService.getVentReportPeriodForm(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId));

    var modelAndView = getVentReportPeriodModelAndView(applicationId);

    modelAndView.addObject("form", reportPeriodForm);

    return modelAndView;
  }

  private ModelAndView getVentReportPeriodModelAndView(Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var modelAndView = new ModelAndView("fcs/vent/ventReportPeriodForm");

    modelAndView
        .addObject("reportEndMonthsMap", DateUtils.monthsMap())
        .addObject("reportEndYearsMap",
            reportPeriodControllerHelperService.getReportEndYearsMap(applicationVersion))
        .addObject("submitUrl", ReverseRouter.route(on(VentReportPeriodController.class)
            .saveVentReportPeriodForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId, null)));

    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveVentReportPeriodForm(@PathVariable Integer applicationId,
                                               @ModelAttribute("form") FlareVentReportPeriodForm form,
                                               BindingResult bindingResult) {

    reportPeriodFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getVentReportPeriodModelAndView(applicationId);
    }

    ventReportPeriodService.saveVentReportPeriod(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(VentReportController.class).getVentReportForm(applicationId));
  }
}
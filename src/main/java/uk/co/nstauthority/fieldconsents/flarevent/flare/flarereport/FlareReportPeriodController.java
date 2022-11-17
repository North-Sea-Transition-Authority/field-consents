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
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/flare-report/period")
public class FlareReportPeriodController {

  private final ApplicationVersionService applicationVersionService;

  private final FlareReportPeriodService flareReportPeriodService;

  private final FlareReportPeriodFormValidator flareReportPeriodFormValidator;

  private final FlareReportPeriodControllerHelperService flareReportPeriodControllerHelperService;

  private final FlareReportPeriodHelperService flareReportPeriodHelperService;

  @Autowired
  FlareReportPeriodController(ApplicationVersionService applicationVersionService,
                              FlareReportPeriodService flareReportPeriodService,
                              FlareReportPeriodFormValidator flareReportPeriodFormValidator,
                              FlareReportPeriodControllerHelperService flareReportPeriodControllerHelperService,
                              FlareReportPeriodHelperService flareReportPeriodHelperService) {
    this.applicationVersionService = applicationVersionService;
    this.flareReportPeriodService = flareReportPeriodService;
    this.flareReportPeriodFormValidator = flareReportPeriodFormValidator;
    this.flareReportPeriodControllerHelperService = flareReportPeriodControllerHelperService;
    this.flareReportPeriodHelperService = flareReportPeriodHelperService;
  }

  @GetMapping
  public ModelAndView getFlareReportPeriodForm(@PathVariable Integer applicationId) {

    FlareReportPeriodForm flareReportPeriodForm = flareReportPeriodService.getFlareReportPeriodForm(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId));

    var modelAndView = getFlareReportPeriodModelAndView(applicationId);

    modelAndView.addObject("form", flareReportPeriodForm);

    return modelAndView;
  }

  private ModelAndView getFlareReportPeriodModelAndView(Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var modelAndView = new ModelAndView("fcs/flare/flareReportPeriodForm");

    modelAndView
        .addObject("reportPeriodStart",
            DateUtils.format(flareReportPeriodHelperService.getProposedReportStartYearMonth(applicationVersion),
                DateUtils.LONG_MONTH_YEAR))
        .addObject("reportPeriodEnd",
            DateUtils.format(flareReportPeriodHelperService.getProposedReportEndYearMonth(applicationVersion),
                DateUtils.LONG_MONTH_YEAR))
        .addObject("reportEndMonthsMap", DateUtils.monthsMap())
        .addObject("reportEndYearsMap",
            flareReportPeriodControllerHelperService.getReportEndYearsMap(applicationVersion))
        .addObject("submitUrl", ReverseRouter.route(on(FlareReportPeriodController.class)
            .saveFlareReportPeriodForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));

    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveFlareReportPeriodForm(@PathVariable Integer applicationId,
                                                @ModelAttribute("form") FlareReportPeriodForm form,
                                                BindingResult bindingResult) {

    flareReportPeriodFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFlareReportPeriodModelAndView(applicationId);
    }

    flareReportPeriodService.saveFlareReportPeriod(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(FlareReportController.class).getFlareReportForm(applicationId));
  }
}
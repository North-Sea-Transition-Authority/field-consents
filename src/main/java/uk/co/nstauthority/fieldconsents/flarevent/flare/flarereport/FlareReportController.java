package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

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
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/flare-report")
public class FlareReportController {

  private final ApplicationVersionService applicationVersionService;

  private final FlareReportService flareReportService;

  private final FlareReportPeriodService flareReportPeriodService;

  private final FlareReportFormService flareReportFormService;

  private final ApplicationUnitService applicationUnitService;

  @Autowired
  FlareReportController(ApplicationVersionService applicationVersionService,
                        FlareReportService flareReportService,
                        FlareReportPeriodService flareReportPeriodService,
                        FlareReportFormService flareReportFormService,
                        ApplicationUnitService applicationUnitService) {
    this.applicationVersionService = applicationVersionService;
    this.flareReportService = flareReportService;
    this.flareReportPeriodService = flareReportPeriodService;
    this.flareReportFormService = flareReportFormService;
    this.applicationUnitService = applicationUnitService;
  }

  @GetMapping
  public ModelAndView getFlareReportForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    // if there is no flare report period then redirect there
    if (!flareReportPeriodService.flareReportPeriodExists(applicationVersion)) {
      return ReverseRouter.redirect(on(FlareReportPeriodController.class).getFlareReportPeriodForm(applicationId));
    }

    FlareReportForm flareReportForm =
        flareReportService.getFlareReportForm(applicationVersion);

    var modelAndView = getFlareReportModelAndView(applicationId, flareReportForm);
    modelAndView.addObject("form", flareReportForm);
    return modelAndView;
  }

  private ModelAndView getFlareReportModelAndView(Integer applicationId, FlareReportForm flareReportForm) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    String pageTitleYearText = Objects.equals(flareReportForm.getStartYear(), flareReportForm.getEndYear())
        ? flareReportForm.getStartYear() : flareReportForm.getStartYear() + "/" + flareReportForm.getEndYear();
    String pageTitle = "Flare report " + pageTitleYearText;

    ModelAndView modelAndView = new ModelAndView("fcs/flare/flareReportForm");
    modelAndView
        .addObject("pageTitle", pageTitle)
        .addObject("categoryUnit",
            applicationUnitService.getFlareCategoryUnit(applicationVersion).getDisplayName())
        .addObject("periodUrl", ReverseRouter.route(on(FlareReportPeriodController.class)
            .getFlareReportPeriodForm(applicationId)))
        .addObject("submitUrl", ReverseRouter.route(on(FlareReportController.class)
            .saveFlareReportForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveFlareReportForm(@PathVariable Integer applicationId,
                                          @ModelAttribute("form") FlareReportForm form,
                                          BindingResult bindingResult) {

    bindingResult = flareReportFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFlareReportModelAndView(applicationId, form);
    }

    flareReportService.saveFlareReport(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}

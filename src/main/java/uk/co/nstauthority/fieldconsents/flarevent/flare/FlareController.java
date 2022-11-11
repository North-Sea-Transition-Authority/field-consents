package uk.co.nstauthority.fieldconsents.flarevent.flare;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}")
public class FlareController {

  public static final String PAGE_NAME_SUMMARY = "Flares";
  public static final String PAGE_NAME_ADD = "Add flare";
  public static final String PAGE_NAME_EDIT = "Change flare";
  public static final String PAGE_NAME_DELETE = "Delete flare";
  static final String PAGE_TITLE_ATTR_NAME = "pageTitle";

  private final ApplicationVersionService applicationVersionService;
  private final FlareService flareService;
  private final FlareSummaryService flareSummaryService;
  private final FlareFormValidator flareFormValidator;
  private final FlareSetupFormValidator flareSetupFormValidator;

  @Autowired
  public FlareController(ApplicationVersionService applicationVersionService,
                         FlareService flareService,
                         FlareSummaryService flareSummaryService,
                         FlareFormValidator flareFormValidator,
                         FlareSetupFormValidator flareSetupFormValidator) {
    this.applicationVersionService = applicationVersionService;
    this.flareService = flareService;
    this.flareSummaryService = flareSummaryService;
    this.flareFormValidator = flareFormValidator;
    this.flareSetupFormValidator = flareSetupFormValidator;
  }

  @GetMapping("/flares/new")
  public ModelAndView addFlare(@PathVariable Integer applicationId) {

    ModelAndView modelAndView = getEditFlareModelAndView(applicationId, PAGE_NAME_ADD);
    modelAndView.addObject("form", new FlareForm());

    return modelAndView;
  }

  private ModelAndView getEditFlareModelAndView(Integer applicationId, String pageTitle) {
    ModelAndView modelAndView = new ModelAndView("fcs/flare/editFlareForm");
    modelAndView.addObject(PAGE_TITLE_ATTR_NAME, pageTitle)
        .addObject("flareTypes", FlareType.getAllAsMap())
        .addObject("cancelUrl",
            ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationId)));

    return modelAndView;
  }

  @PostMapping("/flares/new")
  public ModelAndView saveNewFlare(@PathVariable Integer applicationId,
                                   @ModelAttribute("form") FlareForm form,
                                   BindingResult bindingResult) {

    flareFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditFlareModelAndView(applicationId, PAGE_NAME_ADD);
    }

    flareService.saveNewFlare(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(FlareController.class).viewFlaresSummary(applicationId));
  }

  @GetMapping("/flares")
  public ModelAndView viewFlaresSummary(@PathVariable Integer applicationId) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    // if there are no flares already on the application form then go to the flare task list
    if (!flareService.flaresExistForApplicationVersion(applicationVersion)) {
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
    }

    ModelAndView modelAndView = getViewFlaresSummaryModelAndView(applicationId);
    modelAndView.addObject("form", new FlareSetupForm());

    return modelAndView;
  }

  private ModelAndView getViewFlaresSummaryModelAndView(Integer applicationId) {
    ModelAndView modelAndView = new ModelAndView("fcs/flare/flaresSummaryForm");
    modelAndView.addObject(PAGE_TITLE_ATTR_NAME, PAGE_NAME_SUMMARY)
        .addObject("flareViews", flareSummaryService.getSummaryViews(
            applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId)))
        .addObject("submitUrl",
            ReverseRouter.route(on(FlareController.class).saveFlaresSummary(applicationId, null, null)));

    return modelAndView;
  }

  @PostMapping("/flares")
  public ModelAndView saveFlaresSummary(@PathVariable Integer applicationId,
                                        @ModelAttribute("form") FlareSetupForm form,
                                        BindingResult bindingResult) {

    flareSetupFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getViewFlaresSummaryModelAndView(applicationId);
    }

    // other flares to add so go to the add flare screen
    if (Boolean.TRUE.equals(form.getHasOtherFlaresToAdd())) {
      return ReverseRouter.redirect(on(FlareController.class).addFlare(applicationId));
    }

    // no other flares to add so go to the task list
    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  @GetMapping("/flares/{flareNo}")
  public ModelAndView editFlare(@PathVariable Integer applicationId,
                                @PathVariable Integer flareNo) {

    var modelAndView = getEditFlareModelAndView(applicationId, PAGE_NAME_EDIT);

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var flare = flareService.getFlareOrError(applicationVersion, flareNo);

    modelAndView.addObject("form", FlareForm.from(flare));

    return modelAndView;
  }

  @PostMapping("/flares/{flareNo}")
  public ModelAndView saveFlare(@PathVariable Integer applicationId,
                                @PathVariable Integer flareNo,
                                @ModelAttribute("form") FlareForm form,
                                BindingResult bindingResult) {

    flareFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditFlareModelAndView(applicationId, PAGE_NAME_EDIT);
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var flare = flareService.getFlareOrError(applicationVersion, flareNo);
    flareService.updateFlareFromForm(flare, form);

    return ReverseRouter.redirect(on(FlareController.class).viewFlaresSummary(applicationId));
  }

  @GetMapping("/flares/{flareNo}/delete")
  public ModelAndView deleteFlareConfirm(@PathVariable Integer applicationId,
                                         @PathVariable Integer flareNo) {
    // Find the flare or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var flare = flareService.getFlareOrError(applicationVersion, flareNo);

    // show the flare delete confirm page
    return new ModelAndView("fcs/flare/deleteFlare")
        .addObject(PAGE_TITLE_ATTR_NAME, PAGE_NAME_DELETE)
        .addObject("flareView", FlareView.from(flare, null))
        .addObject("submitUrl",
            ReverseRouter.route(on(FlareController.class).deleteFlare(applicationId, null, flareNo)))
        .addObject("cancelUrl",
            ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationId))
        );
  }

  @PostMapping("/flares/{flareNo}/delete")
  public ModelAndView deleteFlare(@PathVariable Integer applicationId,
                                  RedirectAttributes redirectAttributes,
                                  @PathVariable Integer flareNo) {
    // Find the flare or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var flare = flareService.getFlareOrError(applicationVersion, flareNo);

    // delete the flare
    flareService.deleteFlare(flare);

    redirectAttributes.addFlashAttribute("successfulDeleteBanner", "Flare has been successfully deleted.");
    if (!flareService.flaresExistForApplicationVersion(applicationVersion)) {
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
    }
    return ReverseRouter.redirect(on(FlareController.class).viewFlaresSummary(applicationId));
  }

}

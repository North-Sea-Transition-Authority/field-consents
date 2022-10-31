package uk.co.nstauthority.fieldconsents.flarevent.vent;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}")
public class VentController {

  public static final String PAGE_NAME_SUMMARY = "Vents";
  public static final String PAGE_NAME_ADD = "Add vent";
  public static final String PAGE_NAME_EDIT = "Change vent";
  public static final String PAGE_NAME_DELETE = "Delete vent";
  static final String PAGE_TITLE_ATTR_NAME = "pageTitle";

  private final ApplicationVersionService applicationVersionService;
  private final VentService ventService;
  private final VentSummaryService ventSummaryService;
  private final VentFormValidator ventFormValidator;
  private final VentSetupFormValidator ventSetupFormValidator;

  @Autowired
  public VentController(ApplicationVersionService applicationVersionService,
                        VentService ventService,
                        VentSummaryService ventSummaryService,
                        VentFormValidator ventFormValidator,
                        VentSetupFormValidator ventSetupFormValidator) {
    this.applicationVersionService = applicationVersionService;
    this.ventService = ventService;
    this.ventSummaryService = ventSummaryService;
    this.ventFormValidator = ventFormValidator;
    this.ventSetupFormValidator = ventSetupFormValidator;
  }

  @GetMapping("/vents/new")
  public ModelAndView addVent(@PathVariable Integer applicationId) {

    ModelAndView modelAndView = getEditVentModelAndView(applicationId, PAGE_NAME_ADD);
    modelAndView.addObject("form", new VentForm());

    return modelAndView;
  }

  private ModelAndView getEditVentModelAndView(Integer applicationId, String pageTitle) {
    ModelAndView modelAndView = new ModelAndView("fcs/vent/editVentForm");
    modelAndView.addObject(PAGE_TITLE_ATTR_NAME, pageTitle)
        .addObject("ventTypes", VentType.getAllAsMap())
        .addObject("cancelUrl",
            ReverseRouter.route(on(VentController.class).viewVentsSummary(applicationId)));

    return modelAndView;
  }

  @PostMapping("/vents/new")
  public ModelAndView saveNewVent(@PathVariable Integer applicationId,
                                  @ModelAttribute("form") VentForm form,
                                  BindingResult bindingResult) {

    ventFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditVentModelAndView(applicationId, PAGE_NAME_ADD);
    }

    ventService.saveNewVent(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(VentController.class).viewVentsSummary(applicationId));
  }

  @GetMapping("/vents")
  public ModelAndView viewVentsSummary(@PathVariable Integer applicationId) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    // if there are no vents already on the application form then go to the temporary task list
    if (!ventService.ventsExistForApplicationVersion(applicationVersion)) {
      return new ModelAndView("fcs/vent/ventApplicationTaskList");
    }

    ModelAndView modelAndView = getViewVentsSummaryModelAndView(applicationId);
    modelAndView.addObject("form", new VentSetupForm());

    return modelAndView;
  }

  private ModelAndView getViewVentsSummaryModelAndView(Integer applicationId) {
    ModelAndView modelAndView = new ModelAndView("fcs/vent/ventsSummaryForm");
    modelAndView.addObject(PAGE_TITLE_ATTR_NAME, PAGE_NAME_SUMMARY)
        .addObject("ventViews", ventSummaryService.getSummaryViews(
            applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId)))
        .addObject("submitUrl",
            ReverseRouter.route(on(VentController.class).saveVentsSummary(applicationId, null, null)));

    return modelAndView;
  }

  @PostMapping("/vents")
  public ModelAndView saveVentsSummary(@PathVariable Integer applicationId,
                                       @ModelAttribute("form") VentSetupForm form,
                                       BindingResult bindingResult) {

    ventSetupFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getViewVentsSummaryModelAndView(applicationId);
    }

    // other vents to add so go to the add vent screen
    if (Boolean.TRUE.equals(form.getHasOtherVentsToAdd())) {
      return ReverseRouter.redirect(on(VentController.class).addVent(applicationId));
    }

    // no other vents to add so go to the task list
    return new ModelAndView("fcs/vent/ventApplicationTaskList");
  }

  @GetMapping("/vents/{ventNo}")
  public ModelAndView editVent(@PathVariable Integer applicationId,
                               @PathVariable Integer ventNo) {

    var modelAndView = getEditVentModelAndView(applicationId, PAGE_NAME_EDIT);

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var vent = ventService.getVentOrError(applicationVersion, ventNo);

    modelAndView.addObject("form", VentForm.from(vent));

    return modelAndView;
  }

  @PostMapping("/vents/{ventNo}")
  public ModelAndView saveVent(@PathVariable Integer applicationId,
                               @PathVariable Integer ventNo,
                               @ModelAttribute("form") VentForm form,
                               BindingResult bindingResult) {

    ventFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditVentModelAndView(applicationId, PAGE_NAME_EDIT);
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var vent = ventService.getVentOrError(applicationVersion, ventNo);
    ventService.updateVentFromForm(vent, form);

    return ReverseRouter.redirect(on(VentController.class).viewVentsSummary(applicationId));
  }

  @GetMapping("/vents/{ventNo}/delete")
  public ModelAndView deleteVentConfirm(@PathVariable Integer applicationId,
                                        @PathVariable Integer ventNo) {
    // Find the vent or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var vent = ventService.getVentOrError(applicationVersion, ventNo);

    // show the vent delete confirm page
    return new ModelAndView("fcs/vent/deleteVent")
        .addObject(PAGE_TITLE_ATTR_NAME, PAGE_NAME_DELETE)
        .addObject("ventView", VentView.from(vent, null))
        .addObject("submitUrl",
            ReverseRouter.route(on(VentController.class)
                .deleteVent(applicationId, ventNo)))
        .addObject("cancelUrl",
            ReverseRouter.route(on(VentController.class).viewVentsSummary(applicationId))
        );
  }

  @PostMapping("/vents/{ventNo}/delete")
  public ModelAndView deleteVent(@PathVariable Integer applicationId,
                                 @PathVariable Integer ventNo) {
    // Find the vent or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var vent = ventService.getVentOrError(applicationVersion, ventNo);

    // delete the vent
    ventService.deleteVent(vent);

    return ReverseRouter.redirect(on(VentController.class).viewVentsSummary(applicationId));
  }

}

package uk.co.nstauthority.fieldconsents.startapplication;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/fields/{fieldId}")
public class StartApplicationFromFieldController {

  private final ApplicationService applicationService;

  private final StartApplicationControllerHelperService startApplicationControllerHelperService;

  private final StartApplicationFormValidator formValidator;

  @Autowired
  public StartApplicationFromFieldController(ApplicationService applicationService,
                                             StartApplicationControllerHelperService startApplicationControllerHelperService,
                                             StartApplicationFormValidator formValidator) {
    this.applicationService = applicationService;
    this.startApplicationControllerHelperService = startApplicationControllerHelperService;
    this.formValidator = formValidator;
  }

  @GetMapping("/start-application-field")
  public ModelAndView getStartApplicationModelAndView(@PathVariable Integer fieldId) {
    ModelAndView modelAndView = getModelAndView(fieldId);
    modelAndView.addObject("form", new StartApplicationForm());
    return modelAndView;
  }

  @NotNull
  private ModelAndView getModelAndView(Integer fieldId) {
    ModelAndView modelAndView = new ModelAndView("fcs/startapplication/startApplication");
    modelAndView.addObject("applicationTypes",
        startApplicationControllerHelperService.getApplicationTypesMap(AssetType.FIELD));
    modelAndView.addObject("createApplicationUrl",
        ReverseRouter.route(on(StartApplicationFromFieldController.class).createNewApplicationOfType(
            fieldId,
            null,
            ReverseRouter.emptyBindingResult())
        )
    );
    modelAndView.addObject("cancelUrl", ReverseRouter.route(on(FieldController.class).manageField(fieldId)));
    return modelAndView;
  }

  @PostMapping("/start-application-field")
  public ModelAndView createNewApplicationOfType(@PathVariable Integer fieldId,
                                                 @ModelAttribute("form") StartApplicationForm form,
                                                 BindingResult bindingResult) {
    formValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(fieldId);
    } else {
      ApplicationType type = form.getApplicationType();
      Application application = applicationService.createNewApplicationForField(type, fieldId).getApplication();
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(application.getId()));
    }
  }
}

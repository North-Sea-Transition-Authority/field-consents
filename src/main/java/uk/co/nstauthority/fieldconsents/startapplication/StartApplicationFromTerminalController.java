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
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/terminal/{terminalId}")
public class StartApplicationFromTerminalController {

  private final ApplicationService applicationService;

  private final StartApplicationControllerHelperService startApplicationControllerHelperService;

  private final StartApplicationFormValidator formValidator;

  @Autowired
  public StartApplicationFromTerminalController(ApplicationService applicationService,
                                                StartApplicationControllerHelperService startApplicationControllerHelperService,
                                                StartApplicationFormValidator formValidator) {
    this.applicationService = applicationService;
    this.startApplicationControllerHelperService = startApplicationControllerHelperService;
    this.formValidator = formValidator;
  }

  @GetMapping("/start-application-terminal")
  public ModelAndView getStartApplicationModelAndView(@PathVariable Integer terminalId) {
    ModelAndView modelAndView = getModelAndView(terminalId);
    modelAndView.addObject("form", new StartApplicationForm());
    return modelAndView;
  }

  @NotNull
  private ModelAndView getModelAndView(Integer terminalId) {
    ModelAndView modelAndView = new ModelAndView("fcs/startapplication/startApplication");
    modelAndView.addObject(
        "applicationTypes",
        startApplicationControllerHelperService.getApplicationTypesMap(AssetType.TERMINAL)
    );
    modelAndView.addObject("createApplicationUrl",
        ReverseRouter.route(on(StartApplicationFromTerminalController.class).createNewApplicationOfType(
            terminalId,
            null,
            ReverseRouter.emptyBindingResult())
        )
    );
    modelAndView.addObject("cancelUrl", ReverseRouter.route(on(TerminalController.class).manageTerminal(terminalId)));
    return modelAndView;
  }

  @PostMapping("/start-application-terminal")
  public ModelAndView createNewApplicationOfType(@PathVariable Integer terminalId,
                                                 @ModelAttribute("form") StartApplicationForm form,
                                                 BindingResult bindingResult) {
    formValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(terminalId);
    } else {
      ApplicationType type = form.getApplicationType();
      Application application = applicationService.createNewApplication(type).getApplication();
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(application.getId()));
    }
  }
}

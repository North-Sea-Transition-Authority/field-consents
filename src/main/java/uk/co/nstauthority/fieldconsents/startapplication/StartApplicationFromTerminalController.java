package uk.co.nstauthority.fieldconsents.startapplication;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.startapplication.StartApplicationControllerHelperService.APPLICATION_TYPE_FLASH_ATTRIBUTE;

import java.util.NoSuchElementException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalController;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitRestController;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/manage-asset/facilities/{terminalId}")
@HasPermission(permissions = RolePermission.CREATE_FCS_APPLICATIONS)
public class StartApplicationFromTerminalController {

  private final ApplicationService applicationService;

  private final StartApplicationControllerHelperService startApplicationControllerHelperService;

  private final StartApplicationFormValidator formValidator;

  private final StartApplicationOperatorFormValidator operatorFormValidator;

  private final OrganisationUnitService organisationUnitService;

  private final TerminalService terminalService;

  private final StartApplicationOperatorFormService startApplicationOperatorFormService;

  @Autowired
  public StartApplicationFromTerminalController(ApplicationService applicationService,
                                                StartApplicationControllerHelperService startApplicationControllerHelperService,
                                                StartApplicationFormValidator formValidator,
                                                StartApplicationOperatorFormValidator operatorFormValidator,
                                                OrganisationUnitService organisationUnitService,
                                                TerminalService terminalService,
                                                StartApplicationOperatorFormService startApplicationOperatorFormService) {
    this.applicationService = applicationService;
    this.startApplicationControllerHelperService = startApplicationControllerHelperService;
    this.formValidator = formValidator;
    this.operatorFormValidator = operatorFormValidator;
    this.organisationUnitService = organisationUnitService;
    this.terminalService = terminalService;
    this.startApplicationOperatorFormService = startApplicationOperatorFormService;
  }

  @GetMapping("/start-application")
  public ModelAndView getStartApplicationForm(@PathVariable Integer terminalId) {
    ModelAndView modelAndView = getStartApplicationFormModelAndView(terminalId);
    modelAndView.addObject("form", new StartApplicationForm());
    return modelAndView;
  }

  @NotNull
  private ModelAndView getStartApplicationFormModelAndView(Integer terminalId) {
    ModelAndView modelAndView = new ModelAndView("fcs/startapplication/startApplication");
    modelAndView.addObject("applicationTypes",
        startApplicationControllerHelperService.getApplicationTypesMap(AssetType.TERMINAL));
    modelAndView.addObject("continueStartApplicationUrl",
        ReverseRouter.route(on(StartApplicationFromTerminalController.class).continueStartApplicationOfType(
            terminalId,
            null,
            ReverseRouter.emptyBindingResult(),
            null)
        )
    );
    modelAndView.addObject("cancelUrl",
        ReverseRouter.route(on(TerminalController.class).manageTerminal(terminalId, null)));
    return modelAndView;
  }

  @PostMapping("/start-application")
  public ModelAndView continueStartApplicationOfType(@PathVariable Integer terminalId,
                                                     @ModelAttribute("form") StartApplicationForm form,
                                                     BindingResult bindingResult,
                                                     RedirectAttributes redirectAttributes) {
    formValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getStartApplicationFormModelAndView(terminalId);
    } else {
      redirectAttributes.addFlashAttribute(APPLICATION_TYPE_FLASH_ATTRIBUTE, form.getApplicationType());
      return ReverseRouter.redirect(on(StartApplicationFromTerminalController.class).getStartApplicationOperatorForm(
          terminalId,
          null
      ));
    }
  }

  @GetMapping("/start-application/operator")
  public ModelAndView getStartApplicationOperatorForm(
      @PathVariable Integer terminalId,
      @ModelAttribute(APPLICATION_TYPE_FLASH_ATTRIBUTE) ApplicationType applicationType
  ) {
    ModelAndView modelAndView = getStartApplicationOperatorModelAndView(terminalId);
    modelAndView.addObject("form", new StartApplicationOperatorForm(applicationType));
    return modelAndView;
  }

  private ModelAndView getStartApplicationOperatorModelAndView(Integer terminalId) {
    ModelAndView modelAndView = new ModelAndView("fcs/startapplication/operatorForm");
    modelAndView
        .addObject("createApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromTerminalController.class).createNewApplication(
                terminalId,
                null,
                ReverseRouter.emptyBindingResult(),
                null)
            )
        )
        .addObject("cancelUrl",
            ReverseRouter.route(on(TerminalController.class).manageTerminal(terminalId, null)))
        .addObject("organisationUnitSearchRestUrl",
            ReverseRouter.route(on(OrganisationUnitRestController.class)
                .getOrganisationUnitsForCreator(null, null)))
        .addObject("prefilledOperator",
            startApplicationOperatorFormService.getPrefilledOperatorForTerminal(terminalId));
    return modelAndView;
  }

  @PostMapping("/start-application/operator")
  public ModelAndView createNewApplication(@PathVariable Integer terminalId,
                                           @ModelAttribute("form") StartApplicationOperatorForm form,
                                           BindingResult bindingResult,
                                           ServiceUserDetail user) {
    operatorFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getStartApplicationOperatorModelAndView(terminalId);
    } else {
      ApplicationType type = form.getApplicationType();
      TerminalWithOperatorJson terminalWithOperatorJson = terminalService.getTerminalWithOperator(terminalId,
          "Lookup terminal prior to creating a terminal application");
      Integer operatorOuId = form.getOrganisationUnitId().getAsInteger().orElseThrow(NoSuchElementException::new);
      OrganisationUnitJson operatorOuJson = organisationUnitService.getOrganisationUnitById(operatorOuId,
          "Lookup organisation unit prior to creating a terminal application");
      Application application = applicationService.createNewApplicationForTerminal(
          type, terminalWithOperatorJson, operatorOuJson, user
      ).getApplication();
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(application.getId()));
    }
  }
}

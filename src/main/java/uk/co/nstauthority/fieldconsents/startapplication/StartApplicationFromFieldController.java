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
import uk.co.nstauthority.fieldconsents.assets.fields.FieldController;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Controller
@RequestMapping("/fields/{fieldId}")
public class StartApplicationFromFieldController {

  private final ApplicationService applicationService;

  private final StartApplicationControllerHelperService startApplicationControllerHelperService;

  private final StartApplicationFormValidator formValidator;

  private final StartApplicationOperatorFormValidator operatorFormValidator;

  private final OrganisationUnitService organisationUnitService;

  private final FieldService fieldService;

  @Autowired
  public StartApplicationFromFieldController(ApplicationService applicationService,
                                             StartApplicationControllerHelperService startApplicationControllerHelperService,
                                             StartApplicationFormValidator formValidator,
                                             StartApplicationOperatorFormValidator operatorFormValidator,
                                             OrganisationUnitService organisationUnitService,
                                             FieldService fieldService) {
    this.applicationService = applicationService;
    this.startApplicationControllerHelperService = startApplicationControllerHelperService;
    this.formValidator = formValidator;
    this.operatorFormValidator = operatorFormValidator;
    this.organisationUnitService = organisationUnitService;
    this.fieldService = fieldService;
  }

  @GetMapping("/start-application")
  public ModelAndView getStartApplicationForm(@PathVariable Integer fieldId) {
    ModelAndView modelAndView = getStartApplicationFormModelAndView(fieldId);
    modelAndView.addObject("form", new StartApplicationForm());
    return modelAndView;
  }

  @NotNull
  private ModelAndView getStartApplicationFormModelAndView(Integer fieldId) {
    ModelAndView modelAndView = new ModelAndView("fcs/startapplication/startApplication");
    modelAndView.addObject("applicationTypes",
        startApplicationControllerHelperService.getApplicationTypesMap(AssetType.FIELD));
    modelAndView.addObject("continueStartApplicationUrl",
        ReverseRouter.route(on(StartApplicationFromFieldController.class).continueStartApplicationOfType(
            fieldId,
            null,
            ReverseRouter.emptyBindingResult(),
            null)
        )
    );
    modelAndView.addObject("cancelUrl", ReverseRouter.route(on(FieldController.class).manageField(fieldId)));
    return modelAndView;
  }

  @PostMapping("/start-application")
  public ModelAndView continueStartApplicationOfType(@PathVariable Integer fieldId,
                                                     @ModelAttribute("form") StartApplicationForm form,
                                                     BindingResult bindingResult,
                                                     RedirectAttributes redirectAttributes) {
    formValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getStartApplicationFormModelAndView(fieldId);
    } else {
      redirectAttributes.addFlashAttribute(APPLICATION_TYPE_FLASH_ATTRIBUTE, form.getApplicationType());
      return ReverseRouter.redirect(on(StartApplicationFromFieldController.class).getStartApplicationOperatorForm(
          fieldId,
          null
      ));
    }
  }

  @GetMapping("/start-application/operator")
  public ModelAndView getStartApplicationOperatorForm(
      @PathVariable Integer fieldId,
      @ModelAttribute(APPLICATION_TYPE_FLASH_ATTRIBUTE) ApplicationType applicationType) {
    ModelAndView modelAndView = getStartApplicationOperatorModelAndView(fieldId);
    modelAndView.addObject("form", new StartApplicationOperatorForm(applicationType));
    return modelAndView;
  }

  private ModelAndView getStartApplicationOperatorModelAndView(Integer fieldId) {
    ModelAndView modelAndView = new ModelAndView("fcs/startapplication/operatorForm");
    modelAndView.addObject("createApplicationUrl",
        ReverseRouter.route(on(StartApplicationFromFieldController.class).createNewApplication(
            fieldId,
            null,
            ReverseRouter.emptyBindingResult())
        )
    );
    modelAndView.addObject("cancelUrl", ReverseRouter.route(on(FieldController.class).manageField(fieldId)));
    return modelAndView;
  }

  @PostMapping("/start-application/operator")
  public ModelAndView createNewApplication(@PathVariable Integer fieldId,
                                           @ModelAttribute("form") StartApplicationOperatorForm form,
                                           BindingResult bindingResult) {
    operatorFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getStartApplicationOperatorModelAndView(fieldId);
    } else {
      ApplicationType type = form.getApplicationType();
      FieldWithOperatorAndLicencesJson fieldWithOperatorAndLicencesJson =
          fieldService.getFieldWithOperatorAndLicences(fieldId,
              "Lookup field prior to creating a field application");
      Integer operatorOuId = form.getOrganisationUnitId().getAsInteger().orElseThrow(NoSuchElementException::new);
      OrganisationUnitJson operatorOuJson = organisationUnitService.getOrganisationUnitById(operatorOuId,
          "Lookup organisation unit prior to creating a field application");
      Application application = applicationService.createNewApplicationForField(type,
          fieldWithOperatorAndLicencesJson,
          operatorOuJson).getApplication();
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(application.getId()));
    }
  }
}

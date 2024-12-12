package uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted.HaveSubmittedController;
import uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose.ProjectPurposeController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanEditApplication;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/eia-direction/does-an-eia-screening-direction-need-to-be-submitted")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@UserCanEditApplication
public class NeedsSubmittingController {

  private final NeedsSubmittingFormValidator validator;
  private final ApplicationVersionService applicationVersionService;
  private final EiaDirectionService eiaDirectionService;

  NeedsSubmittingController(NeedsSubmittingFormValidator validator,
                            ApplicationVersionService applicationVersionService,
                            EiaDirectionService eiaDirectionService) {
    this.validator = validator;
    this.applicationVersionService = applicationVersionService;
    this.eiaDirectionService = eiaDirectionService;
  }

  @GetMapping
  public ModelAndView getForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var eiaDirectionOptional = eiaDirectionService.findEiaDirection(applicationVersion);

    if (eiaDirectionOptional.isEmpty()) {
      return ReverseRouter.redirect(on(ProjectPurposeController.class).getForm(applicationId));
    }

    var eiaDirection = eiaDirectionOptional.get();

    if (Boolean.FALSE.equals(eiaDirection.getForPurposeOfEiaRegs())) {
      return ReverseRouter.redirect(on(ProjectPurposeController.class).getForm(applicationId));
    }

    if (Boolean.TRUE.equals(eiaDirection.getHaveSubmittedEiaDirection())) {
      return ReverseRouter.redirect(on(HaveSubmittedController.class).getForm(applicationId));
    }

    return getModelAndView(applicationId, NeedsSubmittingForm.from(eiaDirection));
  }

  @PostMapping
  ModelAndView saveForm(@PathVariable Integer applicationId,
                        @ModelAttribute("form") NeedsSubmittingForm form,
                        BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    if (bindingResult.hasErrors()) {
      return getModelAndView(applicationId, form);
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    eiaDirectionService.updateEiaDirection(applicationVersion, form);
    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
  }

  private ModelAndView getModelAndView(Integer applicationId, NeedsSubmittingForm form) {
    return new ModelAndView("fcs/application/eia-screening/needs-submitting-form")
        .addObject("form", form)
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null)))
        .addObject("backLinkUrl", ReverseRouter.route(on(HaveSubmittedController.class).getForm(applicationId)));
  }

}

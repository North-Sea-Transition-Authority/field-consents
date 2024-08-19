package uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
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
import uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting.NeedsSubmittingController;
import uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose.ProjectPurposeController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationJson;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/eia-direction/has-a-screening-direction-been-submitted")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class HaveSubmittedController {

  private static final String PREFILL_FORM_PETS_REQUEST_PURPOSE = "Prefilling EIA direction form";

  private final HaveSubmittedFormValidator validator;
  private final ApplicationVersionService applicationVersionService;
  private final EiaDirectionService eiaDirectionService;
  private final PetsApplicationService petsApplicationService;

  HaveSubmittedController(HaveSubmittedFormValidator validator,
                          ApplicationVersionService applicationVersionService,
                          EiaDirectionService eiaDirectionService,
                          PetsApplicationService petsApplicationService) {
    this.validator = validator;
    this.applicationVersionService = applicationVersionService;
    this.eiaDirectionService = eiaDirectionService;
    this.petsApplicationService = petsApplicationService;
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
      return ReverseRouter.redirect(on(NeedsSubmittingController.class).getForm(applicationId));
    }

    var form = HaveSubmittedForm.from(eiaDirection);

    return getModelAndView(applicationId, form);
  }

  @PostMapping
  ModelAndView saveForm(@PathVariable Integer applicationId,
                        @ModelAttribute("form") HaveSubmittedForm form,
                        BindingResult bindingResult) {
    validator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(applicationId, form);
    }

    PetsApplicationJson petsApplicationJson = null;
    if (Objects.nonNull(form.satId())) {
      petsApplicationJson = petsApplicationService.getEiaDirectionById(form.satId(), PREFILL_FORM_PETS_REQUEST_PURPOSE);
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    eiaDirectionService.updateEiaDirection(
        applicationVersion,
        form.haveSubmittedEiaDirection(),
        petsApplicationJson
    );

    if (Boolean.FALSE.equals(form.haveSubmittedEiaDirection())) {
      return ReverseRouter.redirect(on(NeedsSubmittingController.class).getForm(applicationId));
    }

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
  }

  private ModelAndView getModelAndView(Integer applicationId, HaveSubmittedForm form) {
    var prefilledEiaDirectionRef = petsApplicationService.findEiaDirectionById(form.satId(), PREFILL_FORM_PETS_REQUEST_PURPOSE)
        .map(petsApp -> new RestSearchItem(petsApp.getSelectionId(), petsApp.getSelectionText()))
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);

    return new ModelAndView("fcs/application/eia-screening/have-submitted-form")
        .addObject("form", form)
        .addObject("backLinkUrl", ReverseRouter.route(on(ProjectPurposeController.class).getForm(applicationId)))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null)))
        .addObject("petsSearchRestUrl", eiaDirectionService.getEiaDirectionRestUrl())
        .addObject("prefilledEiaDirectionRef", prefilledEiaDirectionRef);
  }
}

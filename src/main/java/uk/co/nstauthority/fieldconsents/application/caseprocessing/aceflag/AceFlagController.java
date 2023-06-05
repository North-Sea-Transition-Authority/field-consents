package uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CHANGE_ACE_STATUS;

import jakarta.validation.Valid;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/change-ace-status")
@ActionEndPoint(CHANGE_ACE_STATUS)
public class AceFlagController {

  private final ApplicationVersionService applicationVersionService;

  private final AceFlagService aceFlagService;

  @Autowired
  public AceFlagController(ApplicationVersionService applicationVersionService,
                           AceFlagService aceFlagService) {
    this.applicationVersionService = applicationVersionService;
    this.aceFlagService = aceFlagService;
  }

  @GetMapping
  public ModelAndView getAceFlagForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getAceFlagModelAndView(applicationId)
        .addObject("form", aceFlagService.getAceFlagForm(applicationVersion));
  }

  private ModelAndView getAceFlagModelAndView(Integer applicationId) {
    return new ModelAndView("fcs/application/changeAceStatus")
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .getApplicationCaseProcessing(applicationId, null)));
  }

  @PostMapping
  public ModelAndView saveAceFlagForm(@PathVariable Integer applicationId,
                                      @Valid @ModelAttribute("form") AceFlagForm form,
                                      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return getAceFlagModelAndView(applicationId);
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    aceFlagService.setAceFlag(applicationVersion, form.getAceFlag());

    return ReverseRouter.redirect(on(ApplicationCaseProcessingController.class)
        .getApplicationCaseProcessing(applicationId, null));
  }
}

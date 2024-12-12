package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import java.util.EnumSet;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.role.HasAnyRegulatorRole;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Controller
@RequestMapping("bulk-case-actions")
@HasAnyRegulatorRole({Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, Role.CASE_MANAGER})
public class BulkCaseActionController {

  private final BulkCaseActionSelectionFormValidator bulkCaseActionSelectionFormValidator;
  private final TeamQueryService teamQueryService;

  BulkCaseActionController(
      BulkCaseActionSelectionFormValidator bulkCaseActionSelectionFormValidator,
      TeamQueryService teamQueryService
  ) {
    this.bulkCaseActionSelectionFormValidator = bulkCaseActionSelectionFormValidator;
    this.teamQueryService = teamQueryService;
  }

  @GetMapping
  public ModelAndView getBulkCaseActions(ServiceUserDetail user) {
    return getModelAndView(user).addObject("form", new BulkCaseActionSelectionForm(null));
  }

  @PostMapping
  ModelAndView getBulkCaseAction(
      @ModelAttribute("form") BulkCaseActionSelectionForm form,
      BindingResult bindingResult,
      ServiceUserDetail user
  ) {
    bulkCaseActionSelectionFormValidator.validate(form, bindingResult);
    if (bindingResult.hasErrors()) {
      return getModelAndView(user);
    }

    return new ModelAndView("redirect:" + form.getAction().getSearchUrl());
  }

  private ModelAndView getModelAndView(ServiceUserDetail user) {
    var regulatorRoles = teamQueryService.getStaticRoles(user, TeamType.REGULATOR);

    if (regulatorRoles.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    var availableActions = EnumSet.allOf(BulkCaseAction.class)
        .stream()
        .filter(action -> regulatorRoles.contains(action.getRequiredRole()))
        .toList();

    if (availableActions.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    if (availableActions.size() == 1) {
      var searchUrl = availableActions.getFirst().getSearchUrl();
      return new ModelAndView("redirect:" + searchUrl);
    }

    return new ModelAndView("fcs/application/bulk-case-actions/actionPicker")
        .addObject("pageTitle", "Bulk case actions")
        .addObject("availableActions", availableActions);
  }

}

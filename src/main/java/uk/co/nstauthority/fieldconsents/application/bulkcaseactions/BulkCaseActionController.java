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
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Controller
@RequestMapping("bulk-case-actions")
@HasPermission(permissions = {RolePermission.ASSIGN_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS})
public class BulkCaseActionController {

  private final BulkCaseActionSelectionFormValidator bulkCaseActionSelectionFormValidator;
  private final RegulatorTeamService regulatorTeamService;
  private final PermissionService permissionService;

  BulkCaseActionController(
      BulkCaseActionSelectionFormValidator bulkCaseActionSelectionFormValidator,
      RegulatorTeamService regulatorTeamService,
      PermissionService permissionService
  ) {
    this.bulkCaseActionSelectionFormValidator = bulkCaseActionSelectionFormValidator;
    this.regulatorTeamService = regulatorTeamService;
    this.permissionService = permissionService;
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
    var regulatorTeam = regulatorTeamService.getRegulatorTeamForUser(user)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    var userRolePermissions = permissionService.getUserPermissionsForTeam(regulatorTeam, user);
    var availableActions = EnumSet.allOf(BulkCaseAction.class)
        .stream()
        .filter(action -> userRolePermissions.containsAll(action.getRequiredPermissions()))
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

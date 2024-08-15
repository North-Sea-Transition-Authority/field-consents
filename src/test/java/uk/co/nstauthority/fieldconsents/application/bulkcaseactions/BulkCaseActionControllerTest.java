package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerSearchController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentsSearchController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ContextConfiguration(classes = BulkCaseActionController.class)
class BulkCaseActionControllerTest extends AbstractControllerTest {

  @MockBean
  private BulkCaseActionSelectionFormValidator bulkCaseActionSelectionFormValidator;

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  private final Team regulatorTeam = new TeamTestUtil.TeamBuilder().withTeamType(TeamType.REGULATOR).build();

  @SecurityTest
  void getBulkCaseActions() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);
    when(regulatorTeamService.getRegulatorTeamForUser(user)).thenReturn(Optional.of(regulatorTeam));

    var permissionsForAllActions = EnumSet.allOf(BulkCaseAction.class)
        .stream()
        .map(BulkCaseAction::getRequiredPermissions)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
    when(permissionService.getUserPermissionsForTeam(regulatorTeam, user)).thenReturn(permissionsForAllActions);

    mockMvc.perform(get(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/actionPicker"))
        .andExpect(model().attribute("pageTitle", "Bulk case actions"))
        .andExpect(model().attribute("availableActions", EnumSet.allOf(BulkCaseAction.class).stream().toList()));
  }

  @SecurityTest
  void getBulkCaseActions_hasOnlyAssignPermissions() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);
    when(regulatorTeamService.getRegulatorTeamForUser(user)).thenReturn(Optional.of(regulatorTeam));

    var permissionsForAllActions = BulkCaseAction.ASSIGN_CASE_OFFICER.getRequiredPermissions();
    when(permissionService.getUserPermissionsForTeam(regulatorTeam, user)).thenReturn(permissionsForAllActions);

    mockMvc.perform(get(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))));
  }

  @SecurityTest
  void getBulkCaseActions_hasOnlyBulkIssueConsentsPermissions() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);
    when(regulatorTeamService.getRegulatorTeamForUser(user)).thenReturn(Optional.of(regulatorTeam));

    var permissionsForAllActions = BulkCaseAction.BULK_ISSUE_CONSENTS.getRequiredPermissions();
    when(permissionService.getUserPermissionsForTeam(regulatorTeam, user)).thenReturn(permissionsForAllActions);

    mockMvc.perform(get(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null))));
  }

  @SecurityTest
  void getBulkCaseActions_hasNoBulkActionPermissions() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);
    when(regulatorTeamService.getRegulatorTeamForUser(user)).thenReturn(Optional.of(regulatorTeam));
    when(permissionService.getUserPermissionsForTeam(regulatorTeam, user)).thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getBulkCaseAction() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);

    var selectedAction = BulkCaseAction.BULK_ISSUE_CONSENTS.name();

    mockMvc.perform(post(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseAction(null, null, null)))
        .with(user(user))
        .with(csrf())
        .param("selectedAction", selectedAction))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null))));

    verify(bulkCaseActionSelectionFormValidator).validate(eq(new BulkCaseActionSelectionForm(selectedAction)), any(BindingResult.class));
  }

  @Test
  void getBulkCaseAction_invalidAction() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.ASSIGN_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS))).thenReturn(true);

    var selectedAction = "never gonna give you up, never gonna let you down, never gonna run around and desert you";

    doAnswer(invocation -> {
      invocation.getArgument(1, BindingResult.class).rejectValue("selectedAction", "invalid", "select a valid action");
      return null;
    })
        .when(bulkCaseActionSelectionFormValidator)
        .validate(eq(new BulkCaseActionSelectionForm(selectedAction)), any(BindingResult.class));

    when(regulatorTeamService.getRegulatorTeamForUser(user)).thenReturn(Optional.of(regulatorTeam));

    var permissionsForAllActions = EnumSet.allOf(BulkCaseAction.class)
        .stream()
        .map(BulkCaseAction::getRequiredPermissions)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
    when(permissionService.getUserPermissionsForTeam(regulatorTeam, user)).thenReturn(permissionsForAllActions);

    mockMvc.perform(post(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseAction(null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("selectedAction", selectedAction))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/actionPicker"));
  }

}
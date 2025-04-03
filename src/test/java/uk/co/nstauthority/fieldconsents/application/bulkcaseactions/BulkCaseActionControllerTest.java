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
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerSearchController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentsSearchController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ContextConfiguration(classes = BulkCaseActionController.class)
class BulkCaseActionControllerTest extends AbstractControllerTest {

  @MockitoBean
  private BulkCaseActionSelectionFormValidator bulkCaseActionSelectionFormValidator;

  private final Team regulatorTeam = TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build();

  @SecurityTest
  void getBulkCaseActions() throws Exception {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, Set.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, Role.CASE_MANAGER)))
        .thenReturn(true);

    when(teamQueryService.getStaticRoles(user, TeamType.REGULATOR))
        .thenReturn(Set.of(Role.CASE_MANAGER, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER));

    mockMvc.perform(get(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/actionPicker"))
        .andExpect(model().attribute("pageTitle", "Bulk case actions"))
        .andExpect(model().attribute("availableActions", EnumSet.allOf(BulkCaseAction.class).stream().toList()));
  }

  @SecurityTest
  void getBulkCaseActions_onlyCaseManager() throws Exception {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, Set.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, Role.CASE_MANAGER)))
        .thenReturn(true);

    when(teamQueryService.getStaticRoles(user, TeamType.REGULATOR))
        .thenReturn(Set.of(Role.CASE_MANAGER));

    mockMvc.perform(get(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))));
  }

  @SecurityTest
  void getBulkCaseActions_hasNoBulkActionPermissions() throws Exception {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, Set.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, Role.CASE_MANAGER)))
        .thenReturn(true);

    when(teamQueryService.getTeamRoles(user)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(regulatorTeam)
            .withRole(Role.TECHNICAL_REVIEWER)
            .build()
    ));

    mockMvc.perform(get(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getBulkCaseAction() throws Exception {
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, Set.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, Role.CASE_MANAGER)))
        .thenReturn(true);

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
    when(teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, Set.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, Role.CASE_MANAGER)))
        .thenReturn(true);

    var selectedAction = "never gonna give you up, never gonna let you down, never gonna run around and desert you";

    doAnswer(invocation -> {
      invocation.getArgument(1, BindingResult.class).rejectValue("selectedAction", "invalid", "select a valid action");
      return null;
    })
        .when(bulkCaseActionSelectionFormValidator)
        .validate(eq(new BulkCaseActionSelectionForm(selectedAction)), any(BindingResult.class));

    when(teamQueryService.getStaticRoles(user, TeamType.REGULATOR))
        .thenReturn(Set.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, Role.CASE_MANAGER));

    mockMvc.perform(post(ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseAction(null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("selectedAction", selectedAction))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/bulk-case-actions/actionPicker"));
  }

}
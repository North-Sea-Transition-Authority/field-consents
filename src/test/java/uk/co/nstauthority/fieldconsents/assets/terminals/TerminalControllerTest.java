package uk.co.nstauthority.fieldconsents.assets.terminals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithNullOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromTerminalController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = TerminalController.class)
public class TerminalControllerTest extends AbstractControllerTest {

  @MockBean
  TerminalService terminalService;

  @MockBean
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @BeforeEach
  void setUp() {
    when(permissionService.hasPermission(user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageTerminal_terminalNoOperator(boolean userHasCreatePermission) throws Exception {

    when(terminalService.getTerminalWithOperator(eq(terminal1JsonWithNullOperator.getId()), any()))
        .thenReturn(terminal1JsonWithNullOperator);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any()))
        .thenReturn(userHasCreatePermission);

    var modelAndView = mockMvc
        .perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithNullOperator.getId(), null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, terminal1JsonWithNullOperator, userHasCreatePermission);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageTerminal_terminalWithOperator(boolean userHasCreatePermission) throws Exception {

    when(terminalService.getTerminalWithOperator(eq(terminal1JsonWithOperator.getId()), any()))
        .thenReturn(terminal1JsonWithOperator);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any()))
        .thenReturn(userHasCreatePermission);

    var modelAndView = mockMvc
        .perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithOperator.getId(), null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, terminal1JsonWithOperator, userHasCreatePermission);
  }

  private void checkModelAsserts(ModelAndView modelAndView,
                                 TerminalWithOperatorJson terminalJson,
                                 boolean userHasCreatePermission) {
    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry("terminalJson", terminalJson)
        .containsEntry("operatorExists", terminalJson.operatorExists())
        .containsEntry("startApplicationEnabled",
            terminalJson.operatorExists() && userHasCreatePermission)
        .containsEntry("operatorName", terminalJson.getOperatorName())
        .containsEntry("startApplicationUrl", ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .getStartApplicationForm(terminalJson.getId())));

  }
}

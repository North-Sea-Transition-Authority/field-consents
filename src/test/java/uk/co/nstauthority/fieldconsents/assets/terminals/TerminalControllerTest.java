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
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CONSENTS;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.ManageAssetService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromTerminalController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = TerminalController.class)
public class TerminalControllerTest extends AbstractControllerTest {

  @MockBean
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @MockBean
  private ManageAssetService manageAssetService;

  @BeforeEach
  void setUp() {
    when(assetAccessService.hasAssetPermission(user, terminal1JsonWithOperator, VIEW_FCS_APPLICATIONS, VIEW_FCS_CONSENTS))
        .thenReturn(true);
  }

  @SecurityTest
  void manageTerminal_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithOperator.getId(), null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void manageTerminal_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    when(assetAccessService.hasAssetPermission(user, terminal1JsonWithOperator, VIEW_FCS_APPLICATIONS, VIEW_FCS_CONSENTS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithOperator.getId(), null)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageTerminal_terminalNoOperator(boolean userHasCreatePermission) throws Exception {
    var applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());

    when(assetAccessService.hasAssetPermission(user, terminal1JsonWithNullOperator, VIEW_FCS_APPLICATIONS, VIEW_FCS_CONSENTS))
        .thenReturn(true);
    when(terminalService.getTerminalWithOperator(eq(terminal1JsonWithNullOperator.getId()), any()))
        .thenReturn(terminal1JsonWithNullOperator);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any(RolePermission[].class)))
        .thenReturn(userHasCreatePermission);

    when(manageAssetService.getApplicationDataItems(AssetKey.from(terminal1JsonWithNullOperator), user))
        .thenReturn(applicationDataItems);

    var modelAndView = mockMvc
        .perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithNullOperator.getId(), null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, terminal1JsonWithNullOperator, userHasCreatePermission, applicationDataItems);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void manageTerminal_terminalWithOperator(boolean userHasCreatePermission) throws Exception {
    var applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());

    when(terminalService.getTerminalWithOperator(eq(terminal1JsonWithOperator.getId()), any()))
        .thenReturn(terminal1JsonWithOperator);

    when(organisationUnitPermissionService.hasOperatorPermission(any(), any(), any(RolePermission[].class)))
        .thenReturn(userHasCreatePermission);

    when(manageAssetService.getApplicationDataItems(AssetKey.from(terminal1JsonWithOperator), user))
        .thenReturn(applicationDataItems);

    var modelAndView = mockMvc
        .perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithOperator.getId(), null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView, terminal1JsonWithOperator, userHasCreatePermission, applicationDataItems);
  }

  private void checkModelAsserts(
      ModelAndView modelAndView,
      TerminalWithOperatorJson terminalJson,
      boolean userHasCreatePermission,
      List<ApplicationDataItem> applicationDataItems
  ) {
    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry("terminalJson", terminalJson)
        .containsEntry("operatorExists", terminalJson.operatorExists())
        .containsEntry("startApplicationEnabled",
            terminalJson.operatorExists() && userHasCreatePermission)
        .containsEntry("operatorName", terminalJson.getOperatorName())
        .containsEntry("startApplicationUrl", ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .getStartApplicationForm(terminalJson.getId())))
        .containsEntry("applicationDataItems", applicationDataItems);
  }
}

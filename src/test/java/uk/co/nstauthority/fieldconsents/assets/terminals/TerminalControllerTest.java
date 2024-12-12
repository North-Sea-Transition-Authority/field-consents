package uk.co.nstauthority.fieldconsents.assets.terminals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithNullOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.ManageAssetService;
import uk.co.nstauthority.fieldconsents.assets.StartApplicationDecision;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromTerminalController;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ContextConfiguration(classes = TerminalController.class)
public class TerminalControllerTest extends AbstractControllerTest {

  private static final Set<Role> INDUSTRY_ROLES = EnumSet.of(
      Role.CREATOR,
      Role.EDITOR,
      Role.SUBMITTER,
      Role.FINANCE_ADMINISTRATOR,
      Role.VIEWER,
      Role.CONSENT_RECIPIENT
  );

  @MockBean
  private ManageAssetService manageAssetService;

  @MockBean
  private AssetService assetService;

  @Captor
  private ArgumentCaptor<Supplier<TerminalWithOperatorJson>> terminalJsonSupplierCaptor;

  @SecurityTest
  void manageTerminal_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithOperator.getId(), null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void manageTerminal_whenTerminalNotFound() throws Exception {
    var terminalId = terminal1JsonWithOperator.getId();

    when(terminalService.findTerminalWithOperator(eq(terminalId), anyString()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminalId, null)))
            .with(user(user))
        )
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void manageTerminal_whenUserDoesNotHavePermission_thenIsForbidden() throws Exception {
    var terminalId = terminal1JsonWithOperator.getId();

    when(terminalService.findTerminalWithOperator(eq(terminalId), anyString()))
        .thenReturn(Optional.of(terminal1JsonWithOperator));

    mockMvc.perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminal1JsonWithOperator.getId(), null)))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void manageTerminal() throws Exception {
    var terminalJson = terminal1JsonWithNullOperator;
    var terminalId = terminalJson.getId();

    // Required for AssetRoleInterceptor
    when(terminalService.findTerminalWithOperator(eq(terminalId), anyString()))
        .thenReturn(Optional.of(terminalJson));
    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, terminalJson, INDUSTRY_ROLES))
        .thenReturn(true);

    when(terminalService.getTerminalWithOperator(eq(terminalJson.getId()), any())).thenReturn(terminalJson);

    var startApplicationDecision = StartApplicationDecision.notAllowed(List.of("no operator for this terminal"));
    when(assetService.getStartApplicationDecisionForTerminal(eq(user), terminalJsonSupplierCaptor.capture())).thenReturn(startApplicationDecision);

    var applicationDataItemViews = List.of(ApplicationDataItemUtil.getApplicationDataItemView());
    when(manageAssetService.getApplicationDataItemViews(terminalJson.getAssetKey(), user))
        .thenReturn(applicationDataItemViews);

    var backLinkUrl = ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection());
    var startApplicationUrl = ReverseRouter.route(on(StartApplicationFromTerminalController.class).getStartApplicationForm(terminalJson.getId(), null));

    mockMvc.perform(get(ReverseRouter.route(on(TerminalController.class)
            .manageTerminal(terminalJson.getId(), null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andExpect(model().attribute("terminalJson", terminalJson))
        .andExpect(model().attribute("startApplicationDecision", startApplicationDecision))
        .andExpect(model().attribute("operatorName", terminalJson.getOperatorName()))
        .andExpect(model().attribute("backLinkUrl", backLinkUrl))
        .andExpect(model().attribute("startApplicationUrl", startApplicationUrl))
        .andExpect(model().attribute("applicationDataItemViews", applicationDataItemViews));

    verify(assetService).getStartApplicationDecisionForTerminal(eq(user), terminalJsonSupplierCaptor.capture());
    assertThat(terminalJsonSupplierCaptor.getValue().get()).isEqualTo(terminalJson);
  }
}

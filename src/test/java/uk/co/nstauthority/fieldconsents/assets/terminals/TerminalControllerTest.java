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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromTerminalController;

@WithMockUser
@ContextConfiguration(classes = TerminalController.class)
public class TerminalControllerTest extends AbstractControllerTest {

  @MockBean
  TerminalService terminalService;

  @Test
  void manageTerminal_terminalNoOperator() throws Exception {

    when(terminalService.getTerminalWithOperator(eq(terminal1JsonWithNullOperator.getId()), any()))
        .thenReturn(terminal1JsonWithNullOperator);

    var modelAndView = mockMvc
        .perform(get(ReverseRouter.route(on(TerminalController.class).manageTerminal(terminal1JsonWithNullOperator.getId()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView,
        terminal1JsonWithNullOperator.getId(),
        terminal1JsonWithNullOperator.getName(),
        terminal1JsonWithNullOperator.operatorExists()
    );
  }

  @Test
  void manageTerminal_terminalWithOperator() throws Exception {

    when(terminalService.getTerminalWithOperator(eq(terminal1JsonWithOperator.getId()), any()))
        .thenReturn(terminal1JsonWithOperator);

    var modelAndView = mockMvc
        .perform(get(ReverseRouter.route(on(TerminalController.class).manageTerminal(terminal1JsonWithOperator.getId()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andReturn().getModelAndView();

    checkModelAsserts(modelAndView,
        terminal1JsonWithOperator.getId(),
        terminal1JsonWithOperator.getName(),
        terminal1JsonWithOperator.operatorExists()
    );
  }

  private void checkModelAsserts(ModelAndView modelAndView,
                                 Integer terminalId,
                                 String terminalName,
                                 boolean operatorExists) {
    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry("terminalId", terminalId)
        .containsEntry("terminalName", terminalName)
        .containsEntry("noOperatorExists", !operatorExists)
        .containsEntry("startApplicationUrl", ReverseRouter.route(on(StartApplicationFromTerminalController.class)
            .getStartApplicationForm(terminalId)));

  }
}

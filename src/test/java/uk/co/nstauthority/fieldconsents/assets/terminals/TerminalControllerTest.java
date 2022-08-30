package uk.co.nstauthority.fieldconsents.assets.terminals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@WithMockUser
@ContextConfiguration(classes = TerminalController.class)
public class TerminalControllerTest extends AbstractControllerTest {

  static final TerminalJson TERMINAL1_JSON = new TerminalJson(1, "T1");

  @MockBean
  TerminalService terminalService;

  @Test
  void manageTerminal_terminal1() throws Exception {

    when(terminalService.getTerminalOrError(TERMINAL1_JSON.terminalId())).thenReturn(TERMINAL1_JSON);

    var modelAndView = mockMvc
        .perform(get(ReverseRouter.route(on(TerminalController.class).manageTerminal(TERMINAL1_JSON.terminalId()))))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/assets/terminals"))
        .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertEquals(TERMINAL1_JSON.terminalId(), model.get("terminalId"));
    assertEquals(TERMINAL1_JSON.terminalName(), model.get("terminalName"));
  }

}

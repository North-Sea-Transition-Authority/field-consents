package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@WithMockUser
@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  @Test
  void getWorkArea_assertHttpOk() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/workarea/workArea"));

  }
}
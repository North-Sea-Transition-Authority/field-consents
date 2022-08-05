package uk.co.nstauthority.fieldconsents.dev;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DevController.class)
class DevControllerTest extends AbstractControllerTest {

  @Test
  void getDev() throws Exception {
    mockMvc.perform(get("/").with(user("user")))
        .andExpect(status().isOk())
        .andExpect(view().name("dev/dev-template"));

  }
}

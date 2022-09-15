package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationController;

@ContextConfiguration(classes = StartApplicationController.class)
class StartApplicationControllerTest extends AbstractControllerTest {

  @Test
  @WithMockUser
  void startNewApplication() throws Exception {
    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(StartApplicationController.class).startNewApplication()))
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/start-production-application/"))
            .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();
  }

  @Test
  void startNewApplication_withUnauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartApplicationController.class).startNewApplication()))
        .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}
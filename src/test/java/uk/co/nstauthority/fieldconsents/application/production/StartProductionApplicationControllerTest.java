package uk.co.nstauthority.fieldconsents.application.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.production.StartProductionApplicationController.EXPLORATION_LINK_URL;
import static uk.co.nstauthority.fieldconsents.production.StartProductionApplicationController.FLARE_VENT_LINK_URL;
import static uk.co.nstauthority.fieldconsents.production.StartProductionApplicationController.ONSHORE_LINK_URL;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.StartProductionApplicationController;

@ContextConfiguration(classes = StartProductionApplicationController.class)
class StartProductionApplicationControllerTest extends AbstractControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @Test
  @WithMockUser
  void getStartProductionApplicationModelAndView() throws Exception {
    var modelAndView =
        mockMvc.perform(get(ReverseRouter.route(on(StartProductionApplicationController.class).getStartProductionApplicationModelAndView()))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/startProductionApplication"))
            .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();
    var model = modelAndView.getModel();
    assertEquals(FLARE_VENT_LINK_URL, model.get("flareVentLinkUrl"));
    assertEquals(EXPLORATION_LINK_URL, model.get("explorationLinkUrl"));
    assertEquals(ONSHORE_LINK_URL, model.get("onshoreLinkUrl"));
  }

  @Test
  void getStartProductionApplicationModelAndView_withUnauthorizedUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartProductionApplicationController.class).getStartProductionApplicationModelAndView())))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void startNewProductionApplication() throws Exception {
    var modelAndView =
        mockMvc.perform(post(ReverseRouter.route(on(StartProductionApplicationController.class).startNewProductionApplication()))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("fcs/startapplication/productionApplicationTaskList"))
            .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();
  }

  @Test
  void startNewProductionApplication_withUnauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(StartProductionApplicationController.class).startNewProductionApplication()))
        .with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}
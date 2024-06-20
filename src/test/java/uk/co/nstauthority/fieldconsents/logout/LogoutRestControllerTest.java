package uk.co.nstauthority.fieldconsents.logout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = LogoutRestController.class)
class LogoutRestControllerTest extends AbstractControllerTest {

  private static final Long WUA_ID = 1L;

  @Autowired
  private EnergyPortalConfiguration energyPortalConfiguration;

  @Autowired
  protected WebApplicationContext context;

  @MockBean
  private LogoutService logoutService;

  @SecurityTest
  void logoutService() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(LogoutRestController.class).logoutOfService(null, WUA_ID)))
            .header("Authorization", "Bearer " + energyPortalConfiguration.logoutPreSharedKey()))
        .andExpect(status().isOk());
    verify(logoutService).logoutUser(WUA_ID);
  }

  @SecurityTest
  void logoutService_unauthorized() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(LogoutRestController.class).logoutOfService(null, WUA_ID)))
            .header("Authorization", "Bearer INVALID_KEY"))
        .andExpect(status().isUnauthorized());
    verify(logoutService, never()).logoutUser(any());
  }

  @SecurityTest
  void logoutService_invalidKey() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(LogoutRestController.class).logoutOfService(null, WUA_ID)))
            .header("Authorization", "foo"))
        .andExpect(status().isUnauthorized());
    verify(logoutService, never()).logoutUser(any());
  }
}

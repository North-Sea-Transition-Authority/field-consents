package uk.co.nstauthority.fieldconsents.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = {
    AuthenticationControllerTest.TestAuthenticationController.class
})
class AuthenticationControllerTest extends AbstractControllerTest {

  @Controller
  @RequestMapping("/auth")
  static class TestAuthenticationController {

    @GetMapping("/secured")
    public ModelAndView renderSecured() {
      return new ModelAndView();
    }

  }

  @Test
  void authenticationRequired() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(TestAuthenticationController.class).renderSecured())))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  @WithMockUser
  void authorisedRequest() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(TestAuthenticationController.class).renderSecured())))
        .andExpect(status().isOk());
  }

}

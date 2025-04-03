package uk.co.nstauthority.fieldconsents.mvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.branding.EnableAllBrandingConfigurationProperties;

@WebMvcTest
@ActiveProfiles("test")
@EnableAllBrandingConfigurationProperties
@ContextConfiguration(classes = {
    DefaultPageControllerAdviceTest.TestController.class,
    DefaultPageControllerAdvice.class,
})
class DefaultPageControllerAdviceTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ControllerAdviceService controllerAdviceService;

  @Test
  void addDefaultModelAttributes_verifyDefaultAttributes() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).testEndpoint())).with(user(user)));
    verify(controllerAdviceService).addDefaultModelAttributes(any(Model.class), any(HttpServletRequest.class));
  }

  // Dummy application to stop the @WebMvcTest loading more than it needs
  @SpringBootApplication
  static class TestApplication {

  }

  @Controller
  @RequestMapping("/endpoint")
  static class TestController {

    @GetMapping
    ModelAndView testEndpoint() {
      return new ModelAndView("testTemplate");
    }
  }

}

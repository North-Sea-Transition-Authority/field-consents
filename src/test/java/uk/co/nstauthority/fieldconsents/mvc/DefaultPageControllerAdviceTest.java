package uk.co.nstauthority.fieldconsents.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = {
    DefaultPageControllerAdviceTest.TestController.class,
    DefaultPageControllerAdvice.class
})
class DefaultPageControllerAdviceTest extends AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Test
  void addDefaultModelAttributes_verifyDefaultAttributes() throws Exception {

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(TestController.class).testEndpoint()))
                .with(user(loggedInUser))
        )
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var modelMap = modelAndView.getModel();

    assertThat(modelMap).containsOnlyKeys(
        "customerBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "serviceBranding",
        "org.springframework.validation.BindingResult.serviceBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "loggedInUser",
        "org.springframework.validation.BindingResult.loggedInUser"
    );

    assertThat((CustomerConfigurationProperties) modelMap.get("customerBranding")).hasNoNullFieldsOrProperties();
    assertThat((ServiceConfigurationProperties) modelMap.get("serviceBranding")).hasNoNullFieldsOrProperties();
    assertThat(modelMap).contains(
        entry("serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea())),
        entry("loggedInUser", loggedInUser)
    );
  }


  // Dummy application to stop the @WebMvcTest loading more than it needs
  @SpringBootApplication
  static class TestApplication {
  }

  @RequestMapping("/endpoint")
  static class TestController {

    @GetMapping()
    ModelAndView testEndpoint() {
      return new ModelAndView();
    }
  }

}
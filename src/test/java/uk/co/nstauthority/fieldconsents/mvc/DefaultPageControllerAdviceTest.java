package uk.co.nstauthority.fieldconsents.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.EnableAllBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.topnavigation.TopNavigationService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@WebMvcTest
@ActiveProfiles("test")
@EnableAllBrandingConfigurationProperties
@ContextConfiguration(classes = {
    DefaultPageControllerAdviceTest.TestController.class,
    DefaultPageControllerAdvice.class,
    UserDetailService.class
})
class DefaultPageControllerAdviceTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TopNavigationService topNavigationService;

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
        "customerBrandingConfigurationProperties",
        "org.springframework.validation.BindingResult.customerBrandingConfigurationProperties",
        "serviceBrandingConfigurationProperties",
        "org.springframework.validation.BindingResult.serviceBrandingConfigurationProperties",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "loggedInUser",
        "org.springframework.validation.BindingResult.loggedInUser"
    );

    assertThat(modelMap.get("customerBrandingConfigurationProperties"))
        .asInstanceOf(type(CustomerBrandingConfigurationProperties.class))
        .hasNoNullFieldsOrProperties();

    assertThat(modelMap.get("serviceBrandingConfigurationProperties"))
        .asInstanceOf(type(ServiceBrandingConfigurationProperties.class))
        .hasNoNullFieldsOrProperties();

    assertThat(modelMap).contains(
        entry("serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))),
        entry("loggedInUser", loggedInUser)
    );
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

package uk.co.nstauthority.fieldconsents.fds.footer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.configuration.AccessibilityConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.AnalyticsConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = FooterLinkController.class)
@EnableConfigurationProperties({AccessibilityConfigurationProperties.class, AnalyticsConfigurationProperties.class})
class FooterLinkControllerTest extends AbstractControllerTest {

  @Autowired
  private AnalyticsConfigurationProperties analyticsConfigurationProperties;

  @Test
  void accessibilityStatement() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FooterLinkController.class).accessibilityStatement())))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/accessibility/accessibilityStatement"));
  }

  @Test
  void contactUs() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FooterLinkController.class).contactUs())))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/contact/contact"));
  }

  @Test
  void cookies() throws Exception {
    var expectedServiceAnalyticIdentifier = FooterLinkController.stripGoogleCharactersFromIdentifier(analyticsConfigurationProperties.serviceAnalyticIdentifier());
    var expectedEnergyPortalAnalyticIdentifier = FooterLinkController.stripGoogleCharactersFromIdentifier(analyticsConfigurationProperties.energyPortalAnalyticIdentifier());

    mockMvc.perform(get(ReverseRouter.route(on(FooterLinkController.class).cookies())))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/cookies/cookies"))
        .andExpect(model().attribute("serviceAnalyticIdentifier", expectedServiceAnalyticIdentifier))
        .andExpect(model().attribute("energyPortalAnalyticIdentifier", expectedEnergyPortalAnalyticIdentifier));
  }

  @Test
  void stripGoogleCharactersFromIdentifier() {
    assertThat(FooterLinkController.stripGoogleCharactersFromIdentifier("G-12345")).isEqualTo("12345");
  }
}

package uk.co.nstauthority.fieldconsents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.fieldconsents.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;
import uk.co.nstauthority.fieldconsents.mvc.WithDefaultPageControllerAdvice;

@WebMvcTest
@ActiveProfiles("test")
@IncludeServiceBrandingConfigurationProperties
@WithDefaultPageControllerAdvice
@Import(AbstractControllerTest.TestConfig.class)
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @TestConfiguration
  public static class TestConfig {

    @Bean
    SearchSelectorService searchSelectorService() {
      return new SearchSelectorService();
    }

  }

}

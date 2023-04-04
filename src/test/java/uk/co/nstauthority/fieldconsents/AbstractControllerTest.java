package uk.co.nstauthority.fieldconsents;

import static org.mockito.Mockito.doCallRealMethod;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.fieldconsents.authentication.SamlResponseParser;
import uk.co.nstauthority.fieldconsents.authentication.ServiceLogoutSuccessHandler;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermissionInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.HasTeamPermissionInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.SamlProperties;
import uk.co.nstauthority.fieldconsents.configuration.WebSecurityConfiguration;
import uk.co.nstauthority.fieldconsents.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.fieldconsents.energyportal.IncludeEnergyPortalConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;
import uk.co.nstauthority.fieldconsents.mvc.ErrorListHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.mvc.ResponseBufferSizeHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.mvc.WebMvcConfiguration;
import uk.co.nstauthority.fieldconsents.mvc.WithDefaultPageControllerAdvice;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.PermissionManagementHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.validation.FormErrorSummaryService;
import uk.co.nstauthority.fieldconsents.validation.ValidationErrorOrderingService;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@IncludeServiceBrandingConfigurationProperties
@IncludeEnergyPortalConfigurationProperties
@WithDefaultPageControllerAdvice
@WebMvcTest
@Import({
    AbstractControllerTest.TestConfig.class,
    WebMvcConfiguration.class,
    ErrorListHandlerInterceptor.class,
    FormErrorSummaryService.class,
    ResponseBufferSizeHandlerInterceptor.class,
    PermissionManagementHandlerInterceptor.class,
    HasTeamPermissionInterceptor.class,
    HasPermissionInterceptor.class,
    PermissionService.class,
    WebSecurityConfiguration.class
})
@EnableConfigurationProperties(SamlProperties.class)
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected PermissionService permissionService;

  @Autowired
  protected FormErrorSummaryService formErrorSummaryService;

  @MockBean
  protected TeamMemberService teamMemberService;

  @MockBean
  protected UserDetailService userDetailService;

  @MockBean
  protected SamlResponseParser samlResponseParser;

  @MockBean
  protected ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;

  @BeforeEach
  void setupAbstractControllerTest() {
    doCallRealMethod().when(userDetailService).getUserDetail();
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    SearchSelectorService searchSelectorService() {
      return new SearchSelectorService();
    }

    @Bean
    public ControllerHelperService controllerHelperService() {
      return new ControllerHelperService(validationErrorOrderingService());
    }

    @Bean
    public ValidationErrorOrderingService validationErrorOrderingService() {
      return new ValidationErrorOrderingService(messageSource());
    }

    @Bean("messageSource")
    public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
    }
  }
}

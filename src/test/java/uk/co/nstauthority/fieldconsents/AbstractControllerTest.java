package uk.co.nstauthority.fieldconsents;

import static org.mockito.Mockito.doCallRealMethod;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.authentication.SamlResponseParser;
import uk.co.nstauthority.fieldconsents.authentication.ServiceLogoutSuccessHandler;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailArgumentResolver;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasAssetOrRegulatorRoleInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.UserCanManageAssetsInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.role.StaticRoleHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.EnableAllBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.AccessibilityConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.AnalyticsConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.FeedbackConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.SamlProperties;
import uk.co.nstauthority.fieldconsents.configuration.ServiceConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.WebSecurityConfiguration;
import uk.co.nstauthority.fieldconsents.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.fieldconsents.energyportal.IncludeEnergyPortalConfigurationProperties;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;
import uk.co.nstauthority.fieldconsents.hibernate.HibernateQueryCounter;
import uk.co.nstauthority.fieldconsents.jooq.JooqStatisticsListener;
import uk.co.nstauthority.fieldconsents.metrics.QueryCounter;
import uk.co.nstauthority.fieldconsents.mvc.ControllerAdviceService;
import uk.co.nstauthority.fieldconsents.mvc.ErrorListHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.mvc.PostAuthenticationRequestMdcFilter;
import uk.co.nstauthority.fieldconsents.mvc.RequestLogFilter;
import uk.co.nstauthority.fieldconsents.mvc.ResponseBufferSizeHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.mvc.WebMvcConfiguration;
import uk.co.nstauthority.fieldconsents.mvc.WithDefaultPageControllerAdvice;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementService;
import uk.co.nstauthority.fieldconsents.teams.management.access.TeamManagementHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.topnavigation.TopNavigationService;
import uk.co.nstauthority.fieldconsents.validation.FormErrorSummaryService;
import uk.co.nstauthority.fieldconsents.validation.ValidationErrorOrderingService;

@WebMvcTest
@ActiveProfiles({"development", "test"})
@EnableAllBrandingConfigurationProperties
@IncludeEnergyPortalConfigurationProperties
@WithDefaultPageControllerAdvice
@Import({
    AbstractControllerTest.TestConfig.class,
    WebMvcConfiguration.class,
    ErrorListHandlerInterceptor.class,
    FormErrorSummaryService.class,
    ResponseBufferSizeHandlerInterceptor.class,
    HasAssetOrRegulatorRoleInterceptor.class,
    ApplicationHandlerInterceptor.class,
    WebSecurityConfiguration.class,
    ServiceUserDetailArgumentResolver.class,
    RequestLogFilter.class,
    PostAuthenticationRequestMdcFilter.class,
    TeamManagementHandlerInterceptor.class,
    StaticRoleHandlerInterceptor.class,
    UserCanManageAssetsInterceptor.class
})
@EnableConfigurationProperties({
    SamlProperties.class,
    EnergyPortalConfiguration.class,
    ServiceConfigurationProperties.class,
    AccessibilityConfigurationProperties.class,
    AnalyticsConfigurationProperties.class,
    FeedbackConfigurationProperties.class
})
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @MockitoBean
  protected TeamManagementService teamManagementService;

  @MockitoBean
  protected TeamQueryService teamQueryService;

  @MockitoBean
  protected TopNavigationService topNavigationService;

  @MockitoBean
  protected FieldConsentsAccessService fieldConsentsAccessService;

  @Autowired
  protected FormErrorSummaryService formErrorSummaryService;

  @Autowired
  protected ValidationErrorOrderingService validationErrorOrderingService;

  @MockitoBean
  protected OrganisationUnitService organisationUnitService;

  @MockitoBean
  protected OrganisationGroupQueryService organisationGroupQueryService;

  @MockitoBean
  protected UserDetailService userDetailService;

  @MockitoBean
  protected SamlResponseParser samlResponseParser;

  @MockitoBean
  protected ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;

  @MockitoBean
  protected ApplicationVersionService applicationVersionService;

  @MockitoBean
  protected CaseProcessingActionService caseProcessingActionService;

  @MockitoBean
  protected FieldService fieldService;

  @MockitoBean
  protected TerminalService terminalService;

  @MockitoBean
  protected JooqStatisticsListener jooqStatisticsListener;

  protected ServiceUserDetail user;

  @BeforeEach
  void setupAbstractControllerTest() {
    doCallRealMethod().when(userDetailService).getUserDetail();
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    SearchSelectorService searchSelectorService() {
      return new SearchSelectorService();
    }

    @Bean
    public ControllerHelperService controllerHelperService(ValidationErrorOrderingService validationErrorOrderingService) {
      return new ControllerHelperService(validationErrorOrderingService);
    }

    @Bean
    public ControllerAdviceService controllerAdviceService(
        UserDetailService userDetailService,
        AccessibilityConfigurationProperties accessibilityConfigurationProperties,
        ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
        ServiceConfigurationProperties serviceConfigurationProperties,
        CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties,
        TopNavigationService topNavigationService
    ) {
      return new ControllerAdviceService(
          userDetailService,
          accessibilityConfigurationProperties,
          serviceBrandingConfigurationProperties,
          serviceConfigurationProperties,
          customerBrandingConfigurationProperties,
          topNavigationService
      );
    }

    @Bean
    public ValidationErrorOrderingService validationErrorOrderingService(MessageSource messageSource) {
      return new ValidationErrorOrderingService(messageSource);
    }

    @Bean("messageSource")
    public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
    }

    @Bean
    public QueryCounter queryCounter() {
      return new QueryCounter();
    }

    @Bean
    public HibernateQueryCounter hibernateQueryInterceptor(QueryCounter queryCounter) {
      return new HibernateQueryCounter(queryCounter);
    }
  }
}

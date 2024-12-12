package uk.co.nstauthority.fieldconsents.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailArgumentResolver;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.HasAssetOrRegulatorRoleInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.UserCanManageAssetsInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.role.StaticRoleHandlerInterceptor;
import uk.co.nstauthority.fieldconsents.teams.management.access.TeamManagementHandlerInterceptor;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

  private static final String ASSETS_PATH = "/assets/**";

  private final ErrorListHandlerInterceptor errorListHandlerInterceptor;

  private final ResponseBufferSizeHandlerInterceptor responseBufferSizeHandlerInterceptor;

  private final ApplicationHandlerInterceptor applicationHandlerInterceptor;

  private final ServiceUserDetailArgumentResolver serviceUserDetailArgumentResolver;

  private final TeamManagementHandlerInterceptor teamManagementHandlerInterceptor;

  private final StaticRoleHandlerInterceptor staticRoleHandlerInterceptor;

  private final UserCanManageAssetsInterceptor userCanManageAssetsInterceptor;

  private final HasAssetOrRegulatorRoleInterceptor hasAssetOrRegulatorRoleInterceptor;

  @Autowired
  WebMvcConfiguration(ErrorListHandlerInterceptor errorListHandlerInterceptor,
                      ResponseBufferSizeHandlerInterceptor responseBufferSizeHandlerInterceptor,
                      ApplicationHandlerInterceptor applicationHandlerInterceptor,
                      ServiceUserDetailArgumentResolver serviceUserDetailArgumentResolver,
                      TeamManagementHandlerInterceptor teamManagementHandlerInterceptor,
                      StaticRoleHandlerInterceptor staticRoleHandlerInterceptor,
                      UserCanManageAssetsInterceptor userCanManageAssetsInterceptor,
                      HasAssetOrRegulatorRoleInterceptor hasAssetOrRegulatorRoleInterceptor) {
    this.errorListHandlerInterceptor = errorListHandlerInterceptor;
    this.responseBufferSizeHandlerInterceptor = responseBufferSizeHandlerInterceptor;
    this.applicationHandlerInterceptor = applicationHandlerInterceptor;
    this.serviceUserDetailArgumentResolver = serviceUserDetailArgumentResolver;
    this.teamManagementHandlerInterceptor = teamManagementHandlerInterceptor;
    this.hasAssetOrRegulatorRoleInterceptor = hasAssetOrRegulatorRoleInterceptor;
    this.staticRoleHandlerInterceptor = staticRoleHandlerInterceptor;
    this.userCanManageAssetsInterceptor = userCanManageAssetsInterceptor;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler(ASSETS_PATH)
        .addResourceLocations("classpath:/public/assets/")
        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
        .resourceChain(false)
        .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(responseBufferSizeHandlerInterceptor)
        .excludePathPatterns(ASSETS_PATH);
    registry.addInterceptor(errorListHandlerInterceptor)
        .excludePathPatterns(ASSETS_PATH);
    registry.addInterceptor(applicationHandlerInterceptor)
        .addPathPatterns("/applications/**", "/application-versions/**");
    registry.addInterceptor(userCanManageAssetsInterceptor)
        .addPathPatterns(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()));
    registry.addInterceptor(hasAssetOrRegulatorRoleInterceptor)
        .addPathPatterns("/manage-asset/fields/**", "/manage-asset/facilities/**");
    registry.addInterceptor(teamManagementHandlerInterceptor)
        .addPathPatterns("/team-management/**");
    registry.addInterceptor(staticRoleHandlerInterceptor)
        .excludePathPatterns(ASSETS_PATH, "/api/v1/logout/*", "/error");
  }

  @Bean
  public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
    return new ResourceUrlEncodingFilter();
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(serviceUserDetailArgumentResolver);
  }
}

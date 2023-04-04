package uk.co.nstauthority.fieldconsents.mvc;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermissionInterceptor;
import uk.co.nstauthority.fieldconsents.authorisation.HasTeamPermissionInterceptor;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.PermissionManagementHandlerInterceptor;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

  private static final String ASSETS_PATH = "/assets/**";

  private final ErrorListHandlerInterceptor errorListHandlerInterceptor;

  private final ResponseBufferSizeHandlerInterceptor responseBufferSizeHandlerInterceptor;

  private final PermissionManagementHandlerInterceptor permissionManagementHandlerInterceptor;

  private final HasPermissionInterceptor hasPermissionInterceptor;

  private final HasTeamPermissionInterceptor hasTeamPermissionInterceptor;

  @Autowired
  WebMvcConfiguration(ErrorListHandlerInterceptor errorListHandlerInterceptor,
                      ResponseBufferSizeHandlerInterceptor responseBufferSizeHandlerInterceptor,
                      PermissionManagementHandlerInterceptor permissionManagementHandlerInterceptor,
                      HasPermissionInterceptor hasPermissionInterceptor,
                      HasTeamPermissionInterceptor hasTeamPermissionInterceptor) {
    this.errorListHandlerInterceptor = errorListHandlerInterceptor;
    this.responseBufferSizeHandlerInterceptor = responseBufferSizeHandlerInterceptor;
    this.permissionManagementHandlerInterceptor = permissionManagementHandlerInterceptor;
    this.hasPermissionInterceptor = hasPermissionInterceptor;
    this.hasTeamPermissionInterceptor = hasTeamPermissionInterceptor;
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
    registry.addInterceptor(permissionManagementHandlerInterceptor)
        .addPathPatterns("/permission-management/**");
    registry.addInterceptor(hasTeamPermissionInterceptor)
        .addPathPatterns("/permission-management/**");
    registry.addInterceptor(hasPermissionInterceptor)
        .excludePathPatterns(ASSETS_PATH);
  }

  @Bean
  public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
    return new ResourceUrlEncodingFilter();
  }
}
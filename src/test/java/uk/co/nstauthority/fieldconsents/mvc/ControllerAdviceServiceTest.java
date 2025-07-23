package uk.co.nstauthority.fieldconsents.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.ServiceConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.topnavigation.TopNavigationService;

@ExtendWith(MockitoExtension.class)
class ControllerAdviceServiceTest {

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  @Mock
  private ServiceConfigurationProperties serviceConfigurationProperties;

  @Mock
  private CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @Mock
  private TopNavigationService topNavigationService;

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private ControllerAdviceService controllerAdviceService;

  private final ServiceUserDetail user = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void addDefaultModelAttributes_model_withUser() {
    when(userDetailService.findUserDetail()).thenReturn(Optional.of(user));

    var navigationItems = List.of(TopNavigationItem.WORK_AREA);
    when(topNavigationService.getTopNavigationItems(user)).thenReturn(navigationItems);

    var currentEndpoint = "http://localhost:8080/fcs";
    when(request.getRequestURI()).thenReturn(currentEndpoint);

    var model = new ConcurrentModel();

    controllerAdviceService.addDefaultModelAttributes(model, request);

    assertThat(model.asMap())
        .containsEntry("loggedInUser", user)
        .containsEntry("navigationItems", navigationItems)
        .containsEntry("serviceBrandingConfigurationProperties", serviceBrandingConfigurationProperties)
        .containsEntry("serviceConfigurationProperties", serviceConfigurationProperties)
        .containsEntry("customerBrandingConfigurationProperties", customerBrandingConfigurationProperties)
        .containsEntry("currentEndPoint", currentEndpoint);
  }

  @Test
  void addDefaultModelAttributes_modelAndView_withUser() {
    when(userDetailService.findUserDetail()).thenReturn(Optional.of(user));

    var navigationItems = List.of(TopNavigationItem.WORK_AREA);
    when(topNavigationService.getTopNavigationItems(user)).thenReturn(navigationItems);

    var currentEndpoint = "http://localhost:8080/fcs";
    when(request.getRequestURI()).thenReturn(currentEndpoint);

    var modelAndView = new ModelAndView();

    controllerAdviceService.addDefaultModelAttributes(modelAndView, request);

    assertThat(modelAndView.getModel())
        .containsEntry("loggedInUser", user)
        .containsEntry("navigationItems", navigationItems)
        .containsEntry("serviceBrandingConfigurationProperties", serviceBrandingConfigurationProperties)
        .containsEntry("serviceConfigurationProperties", serviceConfigurationProperties)
        .containsEntry("customerBrandingConfigurationProperties", customerBrandingConfigurationProperties)
        .containsEntry("currentEndPoint", currentEndpoint);
  }

  @Test
  void addDefaultModelAttributes_model_withoutUser() {
    when(userDetailService.findUserDetail()).thenReturn(Optional.empty());

    var navigationItems = List.of(TopNavigationItem.WORK_AREA);
    when(topNavigationService.getTopNavigationItems(null)).thenReturn(navigationItems);

    var currentEndpoint = "http://localhost:8080/fcs";
    when(request.getRequestURI()).thenReturn(currentEndpoint);

    var model = new ConcurrentModel();

    controllerAdviceService.addDefaultModelAttributes(model, request);

    assertThat(model.asMap()).doesNotContainKey("loggedInUser");
  }

  @Test
  void addDefaultModelAttributes_modelAndView_withoutUser() {
    when(userDetailService.findUserDetail()).thenReturn(Optional.empty());

    var navigationItems = List.of(TopNavigationItem.WORK_AREA);
    when(topNavigationService.getTopNavigationItems(null)).thenReturn(navigationItems);

    var currentEndpoint = "http://localhost:8080/fcs";
    when(request.getRequestURI()).thenReturn(currentEndpoint);

    var modelAndView = new ModelAndView();

    controllerAdviceService.addDefaultModelAttributes(modelAndView, request);

    assertThat(modelAndView.getModel()).doesNotContainKey("loggedInUser");
  }

}

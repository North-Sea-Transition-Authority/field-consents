package uk.co.nstauthority.fieldconsents.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.AccessibilityConfigurationProperties;
import uk.co.nstauthority.fieldconsents.configuration.ServiceConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.footer.FooterLink;
import uk.co.nstauthority.fieldconsents.fds.footer.FooterLinkController;
import uk.co.nstauthority.fieldconsents.feedback.FeedbackController;
import uk.co.nstauthority.fieldconsents.topnavigation.TopNavigationService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Service
public class ControllerAdviceService {

  private final UserDetailService userDetailService;
  private final AccessibilityConfigurationProperties accessibilityConfigurationProperties;
  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;
  private final ServiceConfigurationProperties serviceConfigurationProperties;
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;
  private final TopNavigationService topNavigationService;

  public ControllerAdviceService(
      UserDetailService userDetailService,
      AccessibilityConfigurationProperties accessibilityConfigurationProperties,
      ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
      ServiceConfigurationProperties serviceConfigurationProperties,
      CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties,
      TopNavigationService topNavigationService
  ) {
    this.userDetailService = userDetailService;
    this.accessibilityConfigurationProperties = accessibilityConfigurationProperties;
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.serviceConfigurationProperties = serviceConfigurationProperties;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
    this.topNavigationService = topNavigationService;
  }

  public void addDefaultModelAttributes(Object model, HttpServletRequest request) {
    var attributeConsumer = getAttributeConsumer(model);
    var userOptional = userDetailService.findUserDetail();

    userOptional.ifPresent(user -> attributeConsumer.accept("loggedInUser", user));
    var topNavigationItems = topNavigationService.getTopNavigationItems(userOptional.orElse(null));
    attributeConsumer.accept("navigationItems", topNavigationItems);
    attributeConsumer.accept("feedbackUrl", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)));
    attributeConsumer.accept("accessibilityConfigurationProperties", accessibilityConfigurationProperties);
    attributeConsumer.accept("serviceBrandingConfigurationProperties", serviceBrandingConfigurationProperties);
    attributeConsumer.accept("serviceConfigurationProperties", serviceConfigurationProperties);
    attributeConsumer.accept("customerBrandingConfigurationProperties", customerBrandingConfigurationProperties);
    attributeConsumer.accept("serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)));
    attributeConsumer.accept("currentEndPoint", request.getRequestURI());
    attributeConsumer.accept("footerLinks", FooterLink.values());
    attributeConsumer.accept("cookiesStatementUrl", ReverseRouter.route(on(FooterLinkController.class).cookies()));
  }

  private BiConsumer<String, Object> getAttributeConsumer(Object object) {
    if (object instanceof ModelAndView modelAndView) {
      return modelAndView::addObject;
    }

    if (object instanceof Model model) {
      return model::addAttribute;
    }

    throw new IllegalArgumentException("Expected Model or ModelAndView but got %s".formatted(object.getClass().getSimpleName()));
  }

}

package uk.co.nstauthority.fieldconsents.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.co.nstauthority.fieldconsents.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.topnavigation.TopNavigationService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ControllerAdvice
class DefaultPageControllerAdvice {

  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;
  private final TopNavigationService topNavigationService;
  private final UserDetailService userDetailService;

  @Autowired
  DefaultPageControllerAdvice(
      ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
      CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties,
      TopNavigationService topNavigationService,
      UserDetailService userDetailService
  ) {
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
    this.topNavigationService = topNavigationService;
    this.userDetailService = userDetailService;
  }

  @ModelAttribute
  void addDefaultModelAttributes(Model model, HttpServletRequest request) {
    addBrandingAttributes(model);
    addCommonUrls(model);
    addTopNavigationItems(model, request);
    addUser(model);
  }

  @InitBinder
  void initBinder(WebDataBinder binder) {
    // Trim whitespace from form fields
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  private void addBrandingAttributes(Model model) {
    model.addAttribute(
        "serviceBrandingConfigurationProperties",
        serviceBrandingConfigurationProperties
    );
    model.addAttribute(
        "customerBrandingConfigurationProperties",
        customerBrandingConfigurationProperties
    );
  }

  private void addTopNavigationItems(Model model, HttpServletRequest request) {
    model.addAttribute("navigationItems", topNavigationService.getTopNavigationItems());
    model.addAttribute("currentEndPoint", request.getRequestURI());
  }

  private void addCommonUrls(Model model) {
    model.addAttribute("serviceHomeUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)));
  }

  private void addUser(Model model) {
    try {
      var user = userDetailService.getUserDetail();
      model.addAttribute("loggedInUser", user);
    } catch (InvalidAuthenticationException exception) {
      // catch exception as unauthenticated endpoints won't have a logged-in user
    }
  }
}

package uk.co.nstauthority.fieldconsents.fds.footer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.configuration.AnalyticsConfigurationProperties;

@Controller
public class FooterLinkController {

  private final String strippedServiceAnalyticIdentifier;
  private final String strippedEnergyPortalAnalyticIdentifier;

  FooterLinkController(AnalyticsConfigurationProperties analyticsConfig) {
    this.strippedServiceAnalyticIdentifier =
        stripGoogleCharactersFromIdentifier(analyticsConfig.serviceAnalyticIdentifier());
    this.strippedEnergyPortalAnalyticIdentifier =
        stripGoogleCharactersFromIdentifier(analyticsConfig.energyPortalAnalyticIdentifier());
  }

  @GetMapping("/accessibility-statement")
  public ModelAndView accessibilityStatement() {
    return new ModelAndView("fcs/accessibility/accessibilityStatement");
  }

  @GetMapping("/contact-us")
  public ModelAndView contactUs() {
    return new ModelAndView("fcs/contact/contact");
  }

  @GetMapping("/cookies")
  public ModelAndView cookies() {
    return new ModelAndView("fcs/cookies/cookies")
        .addObject("serviceAnalyticIdentifier", strippedServiceAnalyticIdentifier)
        .addObject("energyPortalAnalyticIdentifier", strippedEnergyPortalAnalyticIdentifier);
  }

  static String stripGoogleCharactersFromIdentifier(String identifier) {
    if (StringUtils.isBlank(identifier)) {
      throw new IllegalStateException("No analytic identifier provided");
    }
    return StringUtils.stripStart(identifier, "G-");
  }

}

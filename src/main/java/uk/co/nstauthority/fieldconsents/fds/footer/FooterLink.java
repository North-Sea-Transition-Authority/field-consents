package uk.co.nstauthority.fieldconsents.fds.footer;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.feedback.FeedbackController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public enum FooterLink {

  ACCESSIBILITY_STATEMENT("Accessibility statement",
      ReverseRouter.route(on(FooterLinkController.class).accessibilityStatement())),
  CONTACT_US("Contact us", ReverseRouter.route(on(FooterLinkController.class).contactUs())),
  COOKIES("Cookies", ReverseRouter.route(on(FooterLinkController.class).cookies())),
  FEEDBACK("Feedback", ReverseRouter.route(on(FeedbackController.class).getFeedback(null)))
  ;

  private final String displayText;
  private final String url;

  FooterLink(String displayText, String url) {
    this.displayText = displayText;
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public String getDisplayText() {
    return displayText;
  }
}

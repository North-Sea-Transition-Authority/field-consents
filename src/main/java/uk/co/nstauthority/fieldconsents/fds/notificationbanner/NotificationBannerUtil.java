package uk.co.nstauthority.fieldconsents.fds.notificationbanner;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;

public class NotificationBannerUtil {

  private NotificationBannerUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static void applyNotificationBanner(RedirectAttributes redirectAttributes,
                                             NotificationBanner notificationBanner) {

    redirectAttributes.addFlashAttribute("flash", notificationBanner);
  }

  public static void addSuccessNotification(RedirectAttributes redirectAttributes, String heading, String content) {
    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withContent(content)
        .withTitle(heading)
        .build();

    redirectAttributes.addFlashAttribute("flash", notificationBanner);
  }
}

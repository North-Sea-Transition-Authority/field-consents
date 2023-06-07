package uk.co.nstauthority.fieldconsents.fds.notificationbanner;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;

public class NotificationBannerUtil {

  static final String FLASH_ATTRIBUTE_NAME = "flash";

  private NotificationBannerUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static void applyNotificationBanner(RedirectAttributes redirectAttributes,
                                             NotificationBanner notificationBanner) {

    redirectAttributes.addFlashAttribute(FLASH_ATTRIBUTE_NAME, notificationBanner);
  }

  public static void addSuccessNotification(RedirectAttributes redirectAttributes,
                                            String headingContent) {
    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent(headingContent)
        .build();

    redirectAttributes.addFlashAttribute(FLASH_ATTRIBUTE_NAME, notificationBanner);
  }

  public static void addSuccessNotification(RedirectAttributes redirectAttributes,
                                            String headingContent,
                                            String otherContent) {
    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent(headingContent)
        .withOtherContent(otherContent)
        .build();

    redirectAttributes.addFlashAttribute(FLASH_ATTRIBUTE_NAME, notificationBanner);
  }
}

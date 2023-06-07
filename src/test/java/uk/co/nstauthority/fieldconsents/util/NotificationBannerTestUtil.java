package uk.co.nstauthority.fieldconsents.util;

import static org.springframework.test.util.AssertionErrors.assertEquals;

import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;

public class NotificationBannerTestUtil {

  private NotificationBannerTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static ResultMatcher notificationBanner(NotificationBanner notificationBanner) {
    return result -> {
      var actualBanner = (NotificationBanner) result.getFlashMap().get("flash");
      assertEquals(
          "Failed comparing notification banner title [%s], [%s]"
              .formatted(actualBanner.getTitle(), notificationBanner.getTitle()),
          notificationBanner.getTitle(),
          actualBanner.getTitle()
      );
      assertEquals(
          "Failed comparing notification banner heading [%s], [%s]"
              .formatted(actualBanner.getHeadingContent(), notificationBanner.getHeadingContent()),
          notificationBanner.getHeadingContent(),
          actualBanner.getHeadingContent()
      );
      assertEquals(
          "Failed comparing notification banner content [%s], [%s]"
              .formatted(actualBanner.getOtherContent(), notificationBanner.getOtherContent()),
          notificationBanner.getOtherContent(),
          actualBanner.getOtherContent()
      );
      assertEquals(
          "Failed comparing notification banner type [%s], [%s]"
              .formatted(actualBanner.getType(), notificationBanner.getType()),
          notificationBanner.getType(),
          actualBanner.getType()
      );
    };
  }

}

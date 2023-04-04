package uk.co.nstauthority.fieldconsents.fds.notificationbanner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class NotificationBannerUtilTest {

  @Test
  void applyNotificationBanner() {
    var redirectAttributes = new RedirectAttributesModelMap();
    var notificationBanner = NotificationBanner.builder().build();
    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

    @SuppressWarnings("unchecked")
    var flashAttributes = (Map<String, Object>) redirectAttributes.getFlashAttributes();
    assertThat(flashAttributes).containsEntry("flash", notificationBanner);
  }
}
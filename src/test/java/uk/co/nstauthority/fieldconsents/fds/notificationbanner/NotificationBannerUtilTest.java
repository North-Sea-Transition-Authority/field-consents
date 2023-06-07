package uk.co.nstauthority.fieldconsents.fds.notificationbanner;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil.FLASH_ATTRIBUTE_NAME;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class NotificationBannerUtilTest {

  private static final String SUPPRESS_WARNINGS_UNCHECKED = "unchecked";

  @Test
  void applyNotificationBanner() {
    var redirectAttributes = new RedirectAttributesModelMap();
    var notificationBanner = NotificationBanner.builder().build();
    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

    @SuppressWarnings(SUPPRESS_WARNINGS_UNCHECKED)
    var flashAttributes = (Map<String, Object>) redirectAttributes.getFlashAttributes();
    assertThat(flashAttributes).containsEntry(FLASH_ATTRIBUTE_NAME, notificationBanner);
  }

  @Test
  void addSuccessNotification() {
    var redirectAttributes = new RedirectAttributesModelMap();
    var headingContent = "heading content";
    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent(headingContent)
        .build();

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, headingContent);

    @SuppressWarnings(SUPPRESS_WARNINGS_UNCHECKED)
    var flashAttributes = (Map<String, Object>) redirectAttributes.getFlashAttributes();
    assertThat(flashAttributes)
        .containsKey(FLASH_ATTRIBUTE_NAME);

    assertThat(flashAttributes.get(FLASH_ATTRIBUTE_NAME))
        .usingRecursiveComparison()
        .isEqualTo(expectedNotificationBanner);
  }

  @Test
  void addSuccessNotification_withOtherContent() {
    var redirectAttributes = new RedirectAttributesModelMap();
    var headingContent = "heading content";
    var otherContent = "other content";
    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent(headingContent)
        .withOtherContent(otherContent)
        .build();

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, headingContent, otherContent);

    @SuppressWarnings(SUPPRESS_WARNINGS_UNCHECKED)
    var flashAttributes = (Map<String, Object>) redirectAttributes.getFlashAttributes();
    assertThat(flashAttributes)
        .containsKey(FLASH_ATTRIBUTE_NAME);

    assertThat(flashAttributes.get(FLASH_ATTRIBUTE_NAME))
        .usingRecursiveComparison()
        .isEqualTo(expectedNotificationBanner);
  }
}

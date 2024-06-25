package uk.co.nstauthority.fieldconsents.fds.notificationbanner;

import java.io.Serial;
import java.io.Serializable;

// TODO - Enforce that title and heading fields are provided
public class NotificationBanner implements Serializable {

  @Serial
  private static final long serialVersionUID = 760102195423797103L;

  private final String title;
  private final String headingContent;
  private final String otherContent;
  private final NotificationBannerType type;

  private NotificationBanner(String title, String headingContent, String otherContent, NotificationBannerType type) {
    this.title = title;
    this.headingContent = headingContent;
    this.otherContent = otherContent;
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public String getHeadingContent() {
    return headingContent;
  }

  public String getOtherContent() {
    return otherContent;
  }

  public NotificationBannerType getType() {
    return type;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String title = "";
    private String headingContent = "";
    private String otherContent = "";
    private NotificationBannerType notificationBannerType = NotificationBannerType.INFO;

    private Builder() {
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withHeadingContent(String headingContent) {
      this.headingContent = headingContent;
      return this;
    }

    public Builder withOtherContent(String otherContent) {
      this.otherContent = otherContent;
      return this;
    }

    public Builder withBannerType(NotificationBannerType notificationBannerType) {
      this.notificationBannerType = notificationBannerType;
      if (NotificationBannerType.SUCCESS.equals(this.notificationBannerType)) {
        this.title = "Success";
      }
      return this;
    }

    public NotificationBanner build() {
      return new NotificationBanner(title, headingContent, otherContent, notificationBannerType);
    }
  }
}

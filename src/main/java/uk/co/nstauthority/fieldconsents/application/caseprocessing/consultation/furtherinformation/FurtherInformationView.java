package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record FurtherInformationView(
    String requestedAtTimestamp,
    String requestedByUser,
    String requestText,
    boolean isClosed,
    String respondedAtTimestamp,
    String respondedByUser,
    String responseText
) {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String requestedAtTimestamp;
    private String requestedByUser;
    private String requestText;
    private boolean isClosed;
    private String respondedAtTimestamp;
    private String respondedByUser;
    private String responseText;

    public Builder withRequestedAtTimestamp(@Nullable Instant requestedAtTimestamp) {
      if (Objects.isNull(requestedAtTimestamp)) {
        return this;
      }

      this.requestedAtTimestamp = DateUtils.format(requestedAtTimestamp, DateUtils.DATE_TIME);
      return this;
    }

    public Builder withRequestedByUser(@Nullable EnergyPortalUserDto energyPortalUserDto) {
      if (Objects.isNull(energyPortalUserDto)) {
        return this;
      }

      this.requestedByUser = energyPortalUserDto.displayName();
      return this;
    }

    public Builder withRequestText(String requestText) {
      this.requestText = requestText;
      return this;
    }

    public Builder withStatus(FurtherInformationStatus status) {
      this.isClosed = FurtherInformationStatus.CLOSED.equals(status);
      return this;
    }

    public Builder withRespondedAtTimestamp(@Nullable Instant respondedAtTimestamp) {
      if (Objects.isNull(respondedAtTimestamp)) {
        return this;
      }

      this.respondedAtTimestamp = DateUtils.format(respondedAtTimestamp, DateUtils.DATE_TIME);
      return this;
    }

    public Builder withRespondedByUser(@Nullable EnergyPortalUserDto energyPortalUserDto) {
      if (Objects.isNull(energyPortalUserDto)) {
        return this;
      }

      this.respondedByUser = energyPortalUserDto.displayName();
      return this;
    }

    public Builder withResponseText(String responseText) {
      this.responseText = responseText;
      return this;
    }

    public FurtherInformationView build() {
      return new FurtherInformationView(
          requestedAtTimestamp,
          requestedByUser,
          requestText,
          isClosed,
          respondedAtTimestamp,
          respondedByUser,
          responseText
      );
    }

    private Builder() {
    }

  }

}

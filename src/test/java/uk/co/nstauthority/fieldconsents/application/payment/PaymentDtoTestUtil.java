package uk.co.nstauthority.fieldconsents.application.payment;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;

public class PaymentDtoTestUtil {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private String itemReference = "testItemReference";
    private String itemType = "testItemType";
    private int amountPence = 100;
    private String description = "testDescription";
    private Map<String, Object> metadata = Map.of("testMetadataKey", "testMetadataValue");
    private String returnUrl = "testReturnUrl";
    private String createdByUserId = "testCreatedByUserId";
    private Instant createdInstant = Instant.now();
    private Instant successInstant = Instant.now().plusSeconds(30);
    private String govUkPayId = "testGovUkPayId";
    private String govUkPayNextUrl = "testGovUkPayNextUrl";
    private PaymentStatus status = PaymentStatus.IN_PROGRESS;

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withItemReference(String itemReference) {
      this.itemReference = itemReference;
      return this;
    }

    public Builder withItemType(String itemType) {
      this.itemType = itemType;
      return this;
    }

    public Builder withAmountPence(int amountPence) {
      this.amountPence = amountPence;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder withMetadata(Map<String, Object> metadata) {
      this.metadata = metadata;
      return this;
    }

    public Builder withReturnUrl(String returnUrl) {
      this.returnUrl = returnUrl;
      return this;
    }

    public Builder withCreatedByUserId(String createdByUserId) {
      this.createdByUserId = createdByUserId;
      return this;
    }

    public Builder withCreatedInstant(Instant createdInstant) {
      this.createdInstant = createdInstant;
      return this;
    }

    public Builder withSuccessInstant(Instant successInstant) {
      this.successInstant = successInstant;
      return this;
    }

    public Builder withGovUkPayId(String govUkPayId) {
      this.govUkPayId = govUkPayId;
      return this;
    }

    public Builder withGovUkPayNextUrl(String govUkPayNextUrl) {
      this.govUkPayNextUrl = govUkPayNextUrl;
      return this;
    }

    public Builder withStatus(PaymentStatus status) {
      this.status = status;
      return this;
    }

    public PaymentDto build() {
      return new PaymentDto(
          id,
          itemReference,
          itemType,
          amountPence,
          description,
          metadata,
          returnUrl,
          createdByUserId,
          createdInstant,
          successInstant,
          govUkPayId,
          govUkPayNextUrl,
          status
      );
    }
  }
}

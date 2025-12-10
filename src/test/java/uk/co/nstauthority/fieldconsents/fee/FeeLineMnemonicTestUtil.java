package uk.co.nstauthority.fieldconsents.fee;

import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.revision.ApplicationRevisionType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

public class FeeLineMnemonicTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private AssetType assetType = AssetType.FIELD;
    private ApplicationType applicationType = ApplicationType.PRODUCTION;
    private ConsentLengthType consentLengthType = ConsentLengthType.SHORT_TERM;
    private ApplicationRevisionType applicationRevisionType = ApplicationRevisionType.NEW_CONSENT;

    public Builder withAssetType(AssetType assetType) {
      this.assetType = assetType;
      return this;
    }

    public Builder withApplicationType(ApplicationType applicationType) {
      this.applicationType = applicationType;
      return this;
    }

    public Builder withConsentLengthType(ConsentLengthType consentLengthType) {
      this.consentLengthType = consentLengthType;
      return this;
    }

    public Builder withApplicationRevisionType(ApplicationRevisionType applicationRevisionType) {
      this.applicationRevisionType = applicationRevisionType;
      return this;
    }

    public FeeLineMnemonic build() {
      return FeeLineMnemonic.from(
          assetType,
          applicationType,
          consentLengthType,
          applicationRevisionType
      );
    }
  }
}

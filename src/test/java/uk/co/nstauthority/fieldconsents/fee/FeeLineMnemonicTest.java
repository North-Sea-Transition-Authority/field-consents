package uk.co.nstauthority.fieldconsents.fee;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.revision.ApplicationRevisionType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

class FeeLineMnemonicTest {

  @ParameterizedTest
  @MethodSource("getFromArguments")
  void from_withString(
      AssetType assetType,
      ApplicationType applicationType,
      ConsentLengthType consentLengthType,
      ApplicationRevisionType applicationRevisionType
  ) {
    var mnemonic = "%s/%s/%s/%s".formatted(assetType, applicationType, consentLengthType, applicationRevisionType);

    assertThat(FeeLineMnemonic.from(mnemonic)).isEqualTo(
        new FeeLineMnemonic(
            mnemonic,
            assetType,
            applicationType,
            consentLengthType,
            applicationRevisionType
        )
    );
  }

  @ParameterizedTest
  @MethodSource("getFromArguments")
  void from_withAssetTypeAndApplicationTypeAndConsentLengthTypeAndApplicationRevisionType(
      AssetType assetType,
      ApplicationType applicationType,
      ConsentLengthType consentLengthType,
      ApplicationRevisionType applicationRevisionType
  ) {
    var mnemonic = FeeLineMnemonic.from(assetType, applicationType, consentLengthType, applicationRevisionType);

    assertThat(mnemonic).isEqualTo(
        new FeeLineMnemonic(
            "%s/%s/%s/%s".formatted(assetType, applicationType, consentLengthType, applicationRevisionType),
            assetType,
            applicationType,
            consentLengthType,
            applicationRevisionType
        )
    );
  }

  private static List<Arguments> getFromArguments() {
    var arguments = new ArrayList<Arguments>();

    for (var assetType : AssetType.values()) {
      for (var applicationType : ApplicationType.values()) {
        for (var consentLengthType : ConsentLengthType.values()) {
          for (var applicationRevisionType : ApplicationRevisionType.values()) {
            arguments.add(Arguments.of(assetType, applicationType, consentLengthType, applicationRevisionType));
          }
        }
      }
    }

    return arguments;
  }
}

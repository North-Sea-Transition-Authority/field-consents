package uk.co.nstauthority.fieldconsents.fee;

import java.util.Comparator;
import org.jetbrains.annotations.NotNull;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.consentrevision.ConsentRevisionType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

public record FeeLineMnemonic(
    String mnemonic,
    AssetType assetType,
    ApplicationType applicationType,
    ConsentLengthType consentLengthType,
    ConsentRevisionType consentRevisionType
) implements Comparable<FeeLineMnemonic> {

  private static final Comparator<FeeLineMnemonic> COMPARATOR =
      Comparator.comparing(FeeLineMnemonic::assetType)
          .thenComparing(FeeLineMnemonic::applicationType)
          .thenComparing(FeeLineMnemonic::consentLengthType)
          .thenComparing(FeeLineMnemonic::consentRevisionType);

  public static FeeLineMnemonic from(String mnemonic) {
    var split = mnemonic.split("/");

    return new FeeLineMnemonic(
        mnemonic,
        AssetType.valueOf(split[0]),
        ApplicationType.valueOf(split[1]),
        ConsentLengthType.valueOf(split[2]),
        ConsentRevisionType.valueOf(split[3])
    );
  }

  @Override
  public int compareTo(@NotNull FeeLineMnemonic mnemonic) {
    return COMPARATOR.compare(this, mnemonic);
  }
}

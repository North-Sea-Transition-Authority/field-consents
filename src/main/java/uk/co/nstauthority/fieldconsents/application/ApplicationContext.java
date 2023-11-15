package uk.co.nstauthority.fieldconsents.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;

public record ApplicationContext(
    AssetJson primaryAsset,
    String primaryOperator,
    String applicationVersionStatus,
    String consentStartYear,
    String consentDuration,
    Set<String> assetOperators,
    Set<String> additionalFields,
    Set<String> licences
) {

  public String getPrimaryOperatorPrompt() {
    return "Primary operator";
  }

  public String getStatusPrompt() {
    return "Status";
  }

  public String getStartingYearPrompt() {
    return "Starting year";
  }

  public String getConsentDurationPrompt() {
    return "Consent duration";
  }

  public String getPrimaryAssetPrompt() {
    return switch (primaryAsset.getAssetType()) {
      case FIELD -> "Primary field";
      case TERMINAL -> "Primary facility";
    };
  }

  public String getAssetOperatorsPrompt() {
    var prefix = switch (primaryAsset.getAssetType()) {
      case FIELD -> "Field";
      case TERMINAL -> "Facility";
    };

    if (assetOperators.size() == 1) {
      return "%s operator".formatted(prefix);
    }

    return "%s operators".formatted(prefix);
  }

  public String getAdditionalFieldsPrompt() {
    if (additionalFields.size() == 1) {
      return "Additional field";
    }

    return "Additional fields";
  }

  public String getLicencesPrompt() {
    if (licences.size() == 1) {
      return "Licence";
    }

    return "Licences";
  }

  public String getCommaSeparatedAssetOperators() {
    return getCommaSeperatedString(assetOperators);
  }

  public String getCommaSeparatedAdditionalFields() {
    return getCommaSeperatedString(additionalFields);
  }

  public String getCommaSeparatedLicences() {
    return getCommaSeperatedString(licences);
  }

  private String getCommaSeperatedString(Collection<String> collection) {
    return collection.stream().sorted().collect(Collectors.joining(", "));
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private AssetJson primaryAsset;
    private String primaryOperator;
    private String applicationVersionStatus;
    private String consentStartYear;
    private String consentDuration;
    private Set<String> assetOperators = new HashSet<>();
    private Set<String> additionalFields = new HashSet<>();
    private Set<String> licences = new HashSet<>();

    public Builder withPrimaryAsset(AssetJson primaryAsset) {
      this.primaryAsset = primaryAsset;
      return this;
    }


    public Builder withPrimaryOperator(String primaryOperator) {
      this.primaryOperator = primaryOperator;
      return this;
    }

    public Builder withApplicationVersionStatus(ApplicationVersionStatus status) {
      this.applicationVersionStatus = status.getDisplayName();
      return this;
    }

    public Builder withConsentStartYear(String consentStartYear) {
      this.consentStartYear = consentStartYear;
      return this;
    }

    public Builder withConsentDuration(ConsentLengthType consentLengthType) {
      this.consentDuration = consentLengthType.getDisplayName();
      return this;
    }

    public Builder withAssetOperators(Set<String> assetOperators) {
      this.assetOperators = assetOperators;
      return this;
    }

    public Builder withAdditionalFields(Set<String> additionalFields) {
      this.additionalFields = additionalFields;
      return this;
    }

    public Builder withLicences(Set<String> licences) {
      this.licences = licences;
      return this;
    }

    public ApplicationContext build() {
      return new ApplicationContext(
          primaryAsset,
          primaryOperator,
          applicationVersionStatus,
          consentStartYear,
          consentDuration,
          assetOperators,
          additionalFields,
          licences
      );
    }

    private Builder() {
    }
  }
}

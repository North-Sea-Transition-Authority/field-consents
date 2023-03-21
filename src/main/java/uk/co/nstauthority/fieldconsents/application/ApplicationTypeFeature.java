package uk.co.nstauthority.fieldconsents.application;

import java.util.EnumSet;

public enum ApplicationTypeFeature {

  SECONDARY_ASSETS(EnumSet.of(ApplicationType.FLARE, ApplicationType.VENT)),
  ERAP_SUPPORTING_INFORMATION(EnumSet.of(ApplicationType.FLARE, ApplicationType.VENT)),
  GAS_INJECTION(EnumSet.of(ApplicationType.PRODUCTION)),
  WIDE_SUMMARY_DISPLAY(EnumSet.of(ApplicationType.FLARE, ApplicationType.VENT));

  private final EnumSet<ApplicationType> applicationTypes;

  ApplicationTypeFeature(EnumSet<ApplicationType> applicationTypes) {
    this.applicationTypes = applicationTypes;
  }

  public EnumSet<ApplicationType> getApplicationTypes() {
    return applicationTypes;
  }

  public boolean allowed(ApplicationType type) {
    return this.applicationTypes.contains(type);
  }
}

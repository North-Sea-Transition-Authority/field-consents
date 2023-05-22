package uk.co.nstauthority.fieldconsents.application.assets;

import jakarta.validation.constraints.NotNull;

public class AdditionalAssetsSetupForm {

  @NotNull(message = "Select yes if you want to add other fields to the application")
  private Boolean otherAssetsRequired;

  public Boolean getOtherAssetsRequired() {
    return otherAssetsRequired;
  }

  public void setOtherAssetsRequired(Boolean otherAssetsRequired) {
    this.otherAssetsRequired = otherAssetsRequired;
  }
}

package uk.co.nstauthority.fieldconsents.assets;

import javax.validation.constraints.NotNull;

public class AdditionalAssetsSetupForm {

  @NotNull(message = "Select Yes if you want to add other fields to the application")
  private Boolean otherAssetsRequired;

  public Boolean getOtherAssetsRequired() {
    return otherAssetsRequired;
  }

  public void setOtherAssetsRequired(Boolean otherAssetsRequired) {
    this.otherAssetsRequired = otherAssetsRequired;
  }
}

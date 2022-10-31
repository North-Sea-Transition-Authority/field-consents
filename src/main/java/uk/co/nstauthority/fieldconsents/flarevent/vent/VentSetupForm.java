package uk.co.nstauthority.fieldconsents.flarevent.vent;

public class VentSetupForm {

  private Boolean hasOtherVentsToAdd;

  public VentSetupForm() {
  }

  public VentSetupForm(Boolean hasOtherVentsToAdd) {
    this.hasOtherVentsToAdd = hasOtherVentsToAdd;
  }

  public Boolean getHasOtherVentsToAdd() {
    return hasOtherVentsToAdd;
  }

  public void setHasOtherVentsToAdd(Boolean hasOtherVentsToAdd) {
    this.hasOtherVentsToAdd = hasOtherVentsToAdd;
  }
}

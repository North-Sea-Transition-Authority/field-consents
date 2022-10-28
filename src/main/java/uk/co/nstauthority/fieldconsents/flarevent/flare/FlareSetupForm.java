package uk.co.nstauthority.fieldconsents.flarevent.flare;

public class FlareSetupForm {

  private Boolean hasOtherFlaresToAdd;

  public FlareSetupForm() {
  }

  public FlareSetupForm(Boolean hasOtherFlaresToAdd) {
    this.hasOtherFlaresToAdd = hasOtherFlaresToAdd;
  }

  public Boolean getHasOtherFlaresToAdd() {
    return hasOtherFlaresToAdd;
  }

  public void setHasOtherFlaresToAdd(Boolean hasOtherFlaresToAdd) {
    this.hasOtherFlaresToAdd = hasOtherFlaresToAdd;
  }
}

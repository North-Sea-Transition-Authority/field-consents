package uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag;

import jakarta.validation.constraints.NotNull;

public class AceFlagForm {

  @NotNull(message = "Select yes if this is an ACE application")
  private Boolean aceFlag;

  public Boolean getAceFlag() {
    return aceFlag;
  }

  public void setAceFlag(Boolean aceFlag) {
    this.aceFlag = aceFlag;
  }
}

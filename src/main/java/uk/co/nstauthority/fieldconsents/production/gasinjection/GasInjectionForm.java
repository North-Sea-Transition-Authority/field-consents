package uk.co.nstauthority.fieldconsents.production.gasinjection;

import jakarta.validation.constraints.NotNull;

public class GasInjectionForm {

  @NotNull(message = "Select yes if gas will be injected")
  private Boolean willGasBeInjected;

  public Boolean getWillGasBeInjected() {
    return willGasBeInjected;
  }

  public void setWillGasBeInjected(Boolean willGasBeInjected) {
    this.willGasBeInjected = willGasBeInjected;
  }
}

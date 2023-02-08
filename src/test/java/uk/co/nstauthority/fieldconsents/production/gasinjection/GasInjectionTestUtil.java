package uk.co.nstauthority.fieldconsents.production.gasinjection;

class GasInjectionTestUtil {

  static GasInjectionForm gasInjectionFormStub = new GasInjectionForm();

  static GasInjectionForm getGasInjectionForm(Boolean willGasBeInjected) {
    var gasInjectionForm = new GasInjectionForm();
    gasInjectionForm.setWillGasBeInjected(willGasBeInjected);
    return gasInjectionForm;
  }
}
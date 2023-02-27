package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class EiaDirectionTestUtil {

  static final String WHY_NO_EIA_DIRECTION = "explanation";

  static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

  static EiaDirectionForm getEiaDirectionFormWithSat(Integer satId) {
    var eiaDirectionForm = new EiaDirectionForm();
    eiaDirectionForm.setHaveSubmittedEiaDirection(Boolean.TRUE);
    eiaDirectionForm.setSatId(satId);
    return eiaDirectionForm;
  }

  static EiaDirection getEiaDirectionWithSat(ApplicationVersion applicationVersion,
                                             Integer satId,
                                             String satRef) {
    var eiaDirection = new EiaDirection();
    eiaDirection.setApplicationVersion(applicationVersion);
    eiaDirection.setHaveSubmittedEiaDirection(Boolean.TRUE);
    eiaDirection.setSatId(satId);
    eiaDirection.setCachedSatRef(satRef);
    return eiaDirection;
  }

  static EiaDirectionForm getEiaDirectionFormWithSatToSubmit() {
    var eiaDirectionForm = new EiaDirectionForm();
    eiaDirectionForm.setHaveSubmittedEiaDirection(Boolean.FALSE);
    eiaDirectionForm.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    eiaDirectionForm.getLatestDateToBeSubmitted().setDate(TOMORROW);
    return eiaDirectionForm;
  }


  static EiaDirection getEiaDirectionWithSatToSubmit(ApplicationVersion applicationVersion) {
    var eiaDirection = new EiaDirection();
    eiaDirection.setApplicationVersion(applicationVersion);
    eiaDirection.setHaveSubmittedEiaDirection(Boolean.FALSE);
    eiaDirection.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    eiaDirection.setLatestDateToBeSubmitted(TOMORROW);
    return eiaDirection;
  }

  static EiaDirectionForm getEiaDirectionFormWithNoSatToSubmit() {
    var eiaDirectionForm = new EiaDirectionForm();
    eiaDirectionForm.setHaveSubmittedEiaDirection(Boolean.FALSE);
    eiaDirectionForm.setHaveEiaDirectionToSubmit(Boolean.FALSE);
    eiaDirectionForm.getWhyNoEiaDirection().setInputValue(WHY_NO_EIA_DIRECTION);
    return eiaDirectionForm;
  }

  static EiaDirection getEiaDirectionWithNoSatToSubmit(ApplicationVersion applicationVersion) {
    var eiaDirection = new EiaDirection();
    eiaDirection.setApplicationVersion(applicationVersion);
    eiaDirection.setHaveSubmittedEiaDirection(Boolean.FALSE);
    eiaDirection.setHaveEiaDirectionToSubmit(Boolean.FALSE);
    eiaDirection.setWhyNoEiaDirection(WHY_NO_EIA_DIRECTION);
    return eiaDirection;
  }

  static EiaDirectionForm getEiaDirectionFormWithAllDataSet(Integer satId) {
    var eiaDirectionForm = new EiaDirectionForm();
    eiaDirectionForm.setHaveSubmittedEiaDirection(Boolean.FALSE);
    eiaDirectionForm.setSatId(satId);
    eiaDirectionForm.setHaveEiaDirectionToSubmit(Boolean.FALSE);
    eiaDirectionForm.getLatestDateToBeSubmitted().setDate(TOMORROW);
    eiaDirectionForm.getWhyNoEiaDirection().setInputValue(WHY_NO_EIA_DIRECTION);
    return eiaDirectionForm;
  }

  static EiaDirection getEiaDirectionWithAllDataSet(ApplicationVersion applicationVersion,
                                                    Integer satId,
                                                    String satRef) {
    var eiaDirection = new EiaDirection();
    eiaDirection.setApplicationVersion(applicationVersion);
    eiaDirection.setHaveSubmittedEiaDirection(Boolean.FALSE);
    eiaDirection.setSatId(satId);
    eiaDirection.setCachedSatRef(satRef);
    eiaDirection.setHaveEiaDirectionToSubmit(Boolean.FALSE);
    eiaDirection.setLatestDateToBeSubmitted(TOMORROW);
    eiaDirection.setWhyNoEiaDirection(WHY_NO_EIA_DIRECTION);
    return eiaDirection;
  }
}

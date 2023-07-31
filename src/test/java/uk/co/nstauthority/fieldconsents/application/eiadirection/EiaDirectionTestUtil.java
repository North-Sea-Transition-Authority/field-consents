package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class EiaDirectionTestUtil {

  static final String WHY_NO_EIA_DIRECTION = "explanation";

  static final LocalDate TOMORROW = LocalDate.now().plusDays(1);


  static EiaDirection getEiaDirectionWithSatToSubmit(ApplicationVersion applicationVersion) {
    var eiaDirection = new EiaDirection();
    eiaDirection.setApplicationVersion(applicationVersion);
    eiaDirection.setHaveSubmittedEiaDirection(Boolean.FALSE);
    eiaDirection.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    eiaDirection.setLatestDateToBeSubmitted(TOMORROW);
    return eiaDirection;
  }

}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus.OPEN;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

class ApplicationUpdateTestUtil {

  static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  static final LocalDate CURRENT_DATE = LocalDate.now();

  static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

  static final Instant CURRENT_INSTANT = Instant.now();

  static final int DEADLINE_AHEAD_HOURS = 2;

  static final String APPLICATION_UPDATE_REQUEST_TEXT = "Text request text";

  static ApplicationUpdate getOpenApplicationUpdate(ApplicationVersion applicationVersion,
                                                    Clock clock) {
    var applicationUpdate = new ApplicationUpdate();
    applicationUpdate.setApplicationVersion(applicationVersion);
    applicationUpdate.setApplicationUpdateStatus(OPEN);
    applicationUpdate.setRequestedByWuaId(USER.wuaId());
    applicationUpdate.setRequestedDateTime(clock.instant());
    applicationUpdate.setRequestText(APPLICATION_UPDATE_REQUEST_TEXT);
    applicationUpdate.setDeadlineDateTime(clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return applicationUpdate;
  }
}

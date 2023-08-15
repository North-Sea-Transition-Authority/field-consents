package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus.OPEN;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;

public class ApplicationUpdateTestUtil {

  static final Long UPDATE_REQUESTER_USER_WUA_ID = 1L;

  static final Long UPDATE_RESPONDER_USER_WUA_ID = 2L;

  static final LocalDate CURRENT_DATE = LocalDate.now();

  static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

  static final Instant CURRENT_INSTANT = Instant.now();

  static final int DEADLINE_AHEAD_HOURS = 2;

  static final String APPLICATION_UPDATE_REQUEST_TEXT = "Text request text";

  static final String APPLICATION_UPDATE_RESPONSE_TEXT = "Text response text";

  public static final ApplicationUpdateRequestView applicationUpdateRequestView = new ApplicationUpdateRequestView(
      "requested by user", "xx/xx/xxxx xx:xx", "request text", "xx/xx/xxxx xx:xx"
  );

  static ApplicationUpdate getOpenApplicationUpdate(ApplicationVersion applicationVersion,
                                                    Clock clock) {
    var applicationUpdate = new ApplicationUpdate();
    applicationUpdate.setApplicationVersion(applicationVersion);
    applicationUpdate.setApplicationUpdateStatus(OPEN);
    applicationUpdate.setRequestedByWuaId(UPDATE_REQUESTER_USER_WUA_ID);
    applicationUpdate.setRequestedDateTime(clock.instant());
    applicationUpdate.setRequestText(APPLICATION_UPDATE_REQUEST_TEXT);
    applicationUpdate.setDeadlineDateTime(clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return applicationUpdate;
  }

  static ApplicationUpdate getClosedApplicationUpdate(ApplicationVersion requestApplicationVersion,
                                                      ApplicationVersion responseApplicationVersion,
                                                      ApplicationUpdateResponseType responseType,
                                                      String responseText,
                                                      Clock clock) {
    var applicationUpdate = getOpenApplicationUpdate(requestApplicationVersion, clock);
    applicationUpdate.setResponseType(responseType);
    applicationUpdate.setResponseText(responseText);
    applicationUpdate.setApplicationUpdateStatus(CLOSED);
    applicationUpdate.setResponseApplicationVersion(responseApplicationVersion);
    applicationUpdate.setRespondedByWuaId(UPDATE_RESPONDER_USER_WUA_ID);
    applicationUpdate.setRespondedDateTime(clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return applicationUpdate;
  }
}

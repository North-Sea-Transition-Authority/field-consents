package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus.OPEN;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;

public class ApplicationUpdateTestUtil {

  public static final Long UPDATE_REQUESTER_USER_WUA_ID = 1L;

  static final EnergyPortalUserDto UPDATE_REQUESTER_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(UPDATE_REQUESTER_USER_WUA_ID)
          .withWebUserAccountId(UPDATE_REQUESTER_USER_WUA_ID)
          .withForename("Update Requester Forename")
          .withSurname("Update Requester Surname")
          .build();

  static final Long TECHNICAL_REVIEWER_WUA_ID = 2L;

  static final EnergyPortalUserDto TECHNICAL_REVIEWER_ENERGY_PORTAL_USER_DTO =
      EnergyPortalUserDtoTestUtil.Builder()
          .withId(TECHNICAL_REVIEWER_WUA_ID)
          .withWebUserAccountId(TECHNICAL_REVIEWER_WUA_ID)
          .withForename("Technical Reviewer Forename")
          .withSurname("Technical Reviewer Surname")
          .build();

  public static final Long UPDATE_RESPONDER_USER_WUA_ID = 2L;

  public static final LocalDate CURRENT_DATE = LocalDate.now();

  public static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

  public static final Instant CURRENT_INSTANT = Instant.now();

  public static final int DEADLINE_AHEAD_HOURS = 2;

  public static final String APPLICATION_UPDATE_REQUEST_TEXT = "Text request text";

  public static final String APPLICATION_UPDATE_RESPONSE_TEXT = "Text response text";

  public static final ApplicationUpdateRequestView applicationUpdateRequestView = new ApplicationUpdateRequestView(
      "requested by user", "xx/xx/xxxx xx:xx", "request text", "xx/xx/xxxx xx:xx"
  );

  public static ApplicationUpdate getOpenApplicationUpdate(ApplicationVersion applicationVersion, Clock clock) {
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

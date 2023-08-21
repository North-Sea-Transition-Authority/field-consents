package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

class TechnicalReviewTestUtil {

  static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  static final LocalDate CURRENT_DATE = LocalDate.now();

  static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

  static final Instant CURRENT_INSTANT = Instant.now();

  static final int DEADLINE_AHEAD_HOURS = 2;

  static final String TECHNICAL_REVIEW_REQUEST_TEXT = "Text request text";

  static final String TECHNICAL_REVIEW_RESPONSE_TEXT = "Text response text";

  static TechnicalReview getOpenTechnicalReview(ApplicationVersion applicationVersion) {
    var technicalReview = new TechnicalReview();
    technicalReview.setRequestApplicationVersion(applicationVersion);
    technicalReview.setTechnicalReviewStatus(OPEN);
    technicalReview.setTechnicalReviewerWuaId(2L);
    technicalReview.setRequestedByWuaId(USER.wuaId());
    technicalReview.setRequestedDateTime(Instant.now());
    technicalReview.setRequestText(TECHNICAL_REVIEW_REQUEST_TEXT);
    technicalReview.setDeadlineDateTime(Instant.now().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return technicalReview;
  }

  static TechnicalReview getOpenTechnicalReview(ApplicationVersion applicationVersion,
                                                ServiceUserDetail technicalReviewerUser,
                                                Clock clock) {
    var technicalReview = new TechnicalReview();
    technicalReview.setRequestApplicationVersion(applicationVersion);
    technicalReview.setTechnicalReviewStatus(OPEN);
    technicalReview.setTechnicalReviewerWuaId(technicalReviewerUser.wuaId());
    technicalReview.setRequestedByWuaId(USER.wuaId());
    technicalReview.setRequestedDateTime(clock.instant());
    technicalReview.setRequestText(TECHNICAL_REVIEW_REQUEST_TEXT);
    technicalReview.setDeadlineDateTime(clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return technicalReview;
  }

  static TechnicalReview getClosedTechnicalReviewWithResponseType(ApplicationVersion applicationVersion,
                                                                  TechnicalReviewResponseType technicalReviewResponseType) {
    var technicalReview = getOpenTechnicalReview(applicationVersion);
    technicalReview.setTechnicalReviewStatus(CLOSED);
    technicalReview.setRespondedByWuaId(2L);
    technicalReview.setRespondedDateTime(technicalReview.getRequestedDateTime().plus(1, ChronoUnit.DAYS));
    technicalReview.setResponseText(TECHNICAL_REVIEW_RESPONSE_TEXT);
    technicalReview.setResponseType(technicalReviewResponseType);
    technicalReview.setDeadlineDateTime(Instant.now().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return technicalReview;
  }

  static TechnicalReview getClosedTechnicalReview(ApplicationVersion applicationVersion,
                                                  ServiceUserDetail technicalReviewerUser,
                                                  Clock clock) {
    var technicalReview = getOpenTechnicalReview(applicationVersion, technicalReviewerUser, clock);
    technicalReview.setTechnicalReviewStatus(CLOSED);
    technicalReview.setRespondedByWuaId(technicalReviewerUser.wuaId());
    technicalReview.setRespondedDateTime(technicalReview.getRequestedDateTime().plus(1, ChronoUnit.DAYS));
    technicalReview.setResponseText(TECHNICAL_REVIEW_RESPONSE_TEXT);
    technicalReview.setDeadlineDateTime(clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return technicalReview;
  }
}

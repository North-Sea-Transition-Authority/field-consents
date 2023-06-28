package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;

class TechnicalReviewTestUtil {

  static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  static final Team REGULATOR_TEAM = TeamTestUtil.Builder().build();

  static final LocalDate CURRENT_DATE = LocalDate.now();

  static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

  static final Instant CURRENT_INSTANT = Instant.now();

  static final int DEADLINE_AHEAD_HOURS = 2;

  static final String TECHNICAL_REVIEW_REQUEST_TEXT = "Text request text";

  static TechnicalReview getOpenTechnicalReview(ApplicationVersion applicationVersion,
                                                ServiceUserDetail technicalReviewerUser,
                                                Clock clock) {
    var technicalReview = new TechnicalReview();
    technicalReview.setApplicationVersion(applicationVersion);
    technicalReview.setTechnicalReviewStatus(OPEN);
    technicalReview.setTechnicalReviewerWuaId(technicalReviewerUser.wuaId());
    technicalReview.setRequestedByWuaId(USER.wuaId());
    technicalReview.setRequestedDateTime(clock.instant());
    technicalReview.setRequestText(TECHNICAL_REVIEW_REQUEST_TEXT);
    technicalReview.setDeadlineDateTime(clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS));
    return technicalReview;
  }
}

package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalStatus.REJECTED;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNote;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdate;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawal;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalStatus;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class CaseHistoryEventTestUtil {

  static final String APPLICATION_VERSION_NUMBER = "1";

  static final String CASE_NOTE_EVENT_TEXT = "You have added a new case note.";

  static final String WITHDRAWAL_REQUEST_EVENT_TEXT = "You have requested the withdrawal of this application.";

  static final String WITHDRAWAL_RESPONSE_EVENT_TEXT = "You have responded to the withdrawal request for this application.";

  static final String TECHNICAL_REVIEW_REQUEST_EVENT_TEXT = "You have asked for a technical review.";

  static final String TECHNICAL_REVIEW_RESPONSE_EVENT_TEXT = "You have completed the technical review.";

  static final String MAIN_USER_INVOLVED_FULL_NAME = "Forename Surname";

  static List<CaseEventView> getMockCaseEventViews() {
    return List.of(getCaseEventViewForType(CaseEventType.APPLICATION_CREATED));
  }

  public static CaseEventView getCaseEventViewForType(CaseEventType eventType) {
    var appVersion = new ApplicationVersion();
    appVersion.setVersion(1);
    return new CaseEventView(
        eventType.getCaseEventHeader(),
        eventType.getCaseEventUserLabel(),
        MAIN_USER_INVOLVED_FULL_NAME,
        eventType.getOtherEventUserLabel(),
        null,
        eventType.getCaseEventDateTimeLabel(),
        DateUtils.format(Instant.now(), DateUtils.DATE_TIME),
        APPLICATION_VERSION_NUMBER,
        null,
        null,
        List.of()
    );
  }

  public static CaseEvent getCaseEventForApplicationCreated(ApplicationVersion applicationVersion) {
    return CaseEvent
        .builder(applicationVersion)
        .withEventType(CaseEventType.APPLICATION_CREATED)
        .withMainEventUserWuaId(applicationVersion.getCreatedByWuaId())
        .withEventDateTime(applicationVersion.getCreatedDateTime())
        .build();
  }

  public static CaseEvent getCaseEventForApplicationSubmitted(ApplicationVersion applicationVersion) {
    return CaseEvent
        .builder(applicationVersion)
        .withEventType(CaseEventType.APPLICATION_SUBMITTED)
        .withMainEventUserWuaId(applicationVersion.getSubmittedByWuaId())
        .withEventDateTime(applicationVersion.getSubmittedDateTime())
        .build();
  }

  public static CaseEvent getCaseEventForApplicationUpdateStarted(ApplicationVersion applicationVersion) {
    return CaseEvent
        .builder(applicationVersion)
        .withEventType(CaseEventType.APPLICATION_UPDATE_STARTED)
        .withMainEventUserWuaId(applicationVersion.getCreatedByWuaId())
        .withEventDateTime(applicationVersion.getCreatedDateTime())
        .build();
  }

  public static CaseEvent getApplicationUpdateRequestedEvent(ApplicationUpdate applicationUpdate) {
    var requestText = String.format("Deadline: %s. Update request details: %s",
        DateUtils.format(applicationUpdate.getDeadlineDateTime(), DateUtils.DATE_TIME),
        applicationUpdate.getRequestText());

    return CaseEvent.builder(applicationUpdate.getApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_UPDATE_REQUESTED)
        .withMainEventUserWuaId(applicationUpdate.getRequestedByWuaId())
        .withEventDateTime(applicationUpdate.getRequestedDateTime())
        .withEventText(requestText)
        .build();
  }

  public static CaseEvent getApplicationUpdateSubmittedEvent(ApplicationUpdate applicationUpdate) {
    var responseTypeText = "Update type: " + applicationUpdate.getResponseType().getDisplayName();

    var responseText = Objects.nonNull(applicationUpdate.getResponseText())
        ? ". Update description: %s".formatted(applicationUpdate.getResponseText())
        : "";

    return CaseEvent.builder(applicationUpdate.getResponseApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_UPDATE_SUBMITTED)
        .withMainEventUserWuaId(applicationUpdate.getRespondedByWuaId())
        .withEventDateTime(applicationUpdate.getRespondedDateTime())
        .withEventText(responseTypeText + responseText)
        .build();
  }

  public static CaseEvent getCaseEventForApplicationDeleted(ApplicationVersion applicationVersion, Instant eventDateTime) {
    return CaseEvent
        .builder(applicationVersion)
        .withEventType(CaseEventType.APPLICATION_DELETED)
        .withMainEventUserWuaId(applicationVersion.getCreatedByWuaId())
        .withEventDateTime(eventDateTime)
        .build();
  }

  public static CaseEvent getCaseEventForApplicationUpdateDeleted(ApplicationVersion applicationVersion, Instant eventDateTime) {
    return CaseEvent
        .builder(applicationVersion)
        .withEventType(CaseEventType.DRAFT_APPLICATION_UPDATE_DELETED)
        .withMainEventUserWuaId(applicationVersion.getCreatedByWuaId())
        .withEventDateTime(eventDateTime)
        .build();
  }

  public static CaseEvent getCaseEventForCaseNoteAdded(CaseNote caseNote) {
    return CaseEvent
        .builder(caseNote.getApplicationVersion())
        .withEventType(CaseEventType.CASE_NOTE_ADDED)
        .withMainEventUserWuaId(caseNote.getAddedByWuaId())
        .withEventText(caseNote.getCaseNoteText())
        .withEventDateTime(caseNote.getAddedDateTime())
        .build();
  }

  public static CaseEvent getCaseEventForWithdrawalRequested(ApplicationWithdrawal applicationWithdrawal) {
    return CaseEvent
        .builder(applicationWithdrawal.getApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_WITHDRAWAL_REQUESTED)
        .withMainEventUserWuaId(applicationWithdrawal.getRequestedByWuaId())
        .withEventText(applicationWithdrawal.getRequestText())
        .withEventDateTime(applicationWithdrawal.getRequestedDateTime())
        .build();
  }

  public static CaseEvent getCaseEventForWithdrawalResponded(ApplicationWithdrawal applicationWithdrawal) {
    var withdrawalStatus = applicationWithdrawal.getWithdrawalStatus();

    return CaseEvent
        .builder(applicationWithdrawal.getApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_WITHDRAWAL_RESPONDED)
        .withMainEventUserWuaId(applicationWithdrawal.getRespondedByWuaId())
        .withEventText(
            REJECTED.equals(withdrawalStatus)
                ? "Response: %s. Reject reason: %s"
                .formatted(withdrawalStatus.getDisplayName(),
                    applicationWithdrawal.getResponseText())
                : "Response: %s.".formatted(withdrawalStatus.getDisplayName())
        )
        .withEventDateTime(applicationWithdrawal.getRespondedDateTime())
        .build();
  }

  public static CaseEvent getCaseEventForTechnicalReviewRequested(TechnicalReview technicalReview) {
    return CaseEvent
        .builder(technicalReview.getRequestApplicationVersion())
        .withEventType(CaseEventType.TECHNICAL_REVIEW_REQUESTED)
        .withMainEventUserWuaId(technicalReview.getRequestedByWuaId())
        .withOtherEventUserWuaId(technicalReview.getTechnicalReviewerWuaId())
        .withEventText(String.format(
            "Deadline date time %s. Notes for the reviewer: %s",
            DateUtils.format(technicalReview.getDeadlineDateTime(), DateUtils.DATE_TIME),
            technicalReview.getRequestText())
        )
        .withEventDateTime(technicalReview.getRequestedDateTime())
        .build();
  }

  public static CaseEvent getCaseEventForTechnicalReviewResponded(TechnicalReview technicalReview) {
    var technicalReviewResponseText =
        Objects.nonNull(technicalReview.getResponseText())
            ? "%s: %s".formatted(technicalReview.getResponseType().getResponseTextLabel(), technicalReview.getResponseText())
            : "";

    return CaseEvent
        .builder(technicalReview.getResponseApplicationVersion())
        .withEventType(CaseEventType.TECHNICAL_REVIEW_COMPLETED)
        .withMainEventUserWuaId(technicalReview.getRespondedByWuaId())
        .withEventText(String.format("Decision: %s. %s",
            technicalReview.getResponseType().getDisplayName(),
            technicalReviewResponseText
        ))
        .withEventDateTime(technicalReview.getRespondedDateTime())
        .build();
  }

  public static ApplicationWithdrawal getApplicationWithdrawalRequest(ApplicationVersion applicationVersion) {
    var applicationWithdrawal = new ApplicationWithdrawal();
    applicationWithdrawal.setApplicationVersion(applicationVersion);
    applicationWithdrawal.setRequestedDateTime(applicationVersion.getSubmittedDateTime().plus(3, ChronoUnit.DAYS));
    applicationWithdrawal.setRequestText(WITHDRAWAL_REQUEST_EVENT_TEXT);
    applicationWithdrawal.setRequestedByWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_1.webUserAccountId());
    applicationWithdrawal.setWithdrawalStatus(WithdrawalStatus.OPEN);
    return applicationWithdrawal;
  }

  public static ApplicationWithdrawal getApplicationWithdrawalWithResponse(ApplicationVersion applicationVersion) {
    var applicationWithdrawal = getApplicationWithdrawalRequest(applicationVersion);
    applicationWithdrawal.setRespondedDateTime(applicationVersion.getSubmittedDateTime().plus(4, ChronoUnit.DAYS));
    applicationWithdrawal.setRespondedByWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_2.webUserAccountId());
    applicationWithdrawal.setWithdrawalStatus(WithdrawalStatus.ACCEPTED);
    return applicationWithdrawal;
  }

  public static CaseNote getCaseNote(ApplicationVersion applicationVersion) {
    var caseNote = new CaseNote();
    caseNote.setCaseNoteText(CASE_NOTE_EVENT_TEXT);
    caseNote.setAddedByWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_1.webUserAccountId());
    caseNote.setAddedDateTime(applicationVersion.getSubmittedDateTime().plus(2, ChronoUnit.DAYS));
    caseNote.setApplicationVersion(applicationVersion);
    return caseNote;
  }

  public static TechnicalReview getTechnicalReviewRequest(ApplicationVersion applicationVersion) {
    var technicalReview = new TechnicalReview();
    technicalReview.setRequestApplicationVersion(applicationVersion);
    technicalReview.setRequestedDateTime(applicationVersion.getSubmittedDateTime().plus(5, ChronoUnit.DAYS));
    technicalReview.setRequestText(TECHNICAL_REVIEW_REQUEST_EVENT_TEXT);
    technicalReview.setRequestedByWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_1.webUserAccountId());
    technicalReview.setTechnicalReviewerWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_2.webUserAccountId());
    technicalReview.setTechnicalReviewStatus(TechnicalReviewStatus.OPEN);
    return technicalReview;
  }

  public static TechnicalReview getTechnicalReviewResponse(ApplicationVersion applicationVersion,
                                                           TechnicalReviewResponseType technicalReviewResponseType) {
    var technicalReview = getTechnicalReviewRequest(applicationVersion);
    technicalReview.setRespondedDateTime(applicationVersion.getSubmittedDateTime().plus(6, ChronoUnit.DAYS));
    technicalReview.setResponseText(TECHNICAL_REVIEW_RESPONSE_EVENT_TEXT);
    technicalReview.setRespondedByWuaId(AssignmentTestUtil.ENERGY_PORTAL_USER_2.webUserAccountId());
    technicalReview.setTechnicalReviewStatus(TechnicalReviewStatus.CLOSED);
    technicalReview.setResponseType(technicalReviewResponseType);
    return technicalReview;
  }

  public static Map<WebUserAccountId, EnergyPortalUserDto> getPortalUsersDtosMap() {
    return Map.of(
        WebUserAccountId.from(AssignmentTestUtil.ENERGY_PORTAL_USER_1.webUserAccountId()),
        AssignmentTestUtil.ENERGY_PORTAL_USER_1,
        WebUserAccountId.from(AssignmentTestUtil.ENERGY_PORTAL_USER_2.webUserAccountId()),
        AssignmentTestUtil.ENERGY_PORTAL_USER_2,
        WebUserAccountId.from(AssignmentTestUtil.ENERGY_PORTAL_USER_3.webUserAccountId()),
        AssignmentTestUtil.ENERGY_PORTAL_USER_3
    );
  }
}

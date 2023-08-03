package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import java.time.Clock;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAudit;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;

public class CaseAssignmentEventTestUtil {

  static final Long CASE_OFFICER_WUA_ID_1 = 1L;

  static final Long CASE_OFFICER_WUA_ID_2 = 2L;

  static final Long AUDIT_USER_WUA_ID = 1000L;

  static final Instant DUMMY_INSTANT = Clock.systemDefaultZone().instant();

  static ApplicationVersionAudit getApplicationVersionAuditCaseOwnershipTaken(ApplicationVersion applicationVersion) {
    return new ApplicationVersionAudit(
        applicationVersion.getId(),
        CASE_OFFICER_WUA_ID_1,
        DUMMY_INSTANT,
        CASE_OFFICER_WUA_ID_1
    );
  }

  static CaseEvent getCaseEventOwnershipTaken(ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CASE_OFFICER_OWNERSHIP_TAKEN)
        .withMainEventUserWuaId(CASE_OFFICER_WUA_ID_1)
        .withEventDateTime(DUMMY_INSTANT)
        .build();
  }

  static ApplicationVersionAudit getApplicationVersionAuditCaseOfficerNotAssigned(ApplicationVersion applicationVersion) {
    return new ApplicationVersionAudit(
        applicationVersion.getId(),
        null,
        DUMMY_INSTANT,
        CASE_OFFICER_WUA_ID_1
    );
  }

  static CaseEvent getCaseEventOwnershipReleased(ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CASE_OFFICER_OWNERSHIP_RELEASED)
        .withMainEventUserWuaId(CASE_OFFICER_WUA_ID_1)
        .withEventDateTime(DUMMY_INSTANT)
        .build();
  }

  static ApplicationVersionAudit getApplicationVersionAuditCaseOfficerAssigned(ApplicationVersion applicationVersion, Long caseOfficerWuaId) {
    return new ApplicationVersionAudit(
        applicationVersion.getId(),
        caseOfficerWuaId,
        DUMMY_INSTANT,
        AUDIT_USER_WUA_ID
    );
  }

  static CaseEvent getCaseEventOfficerAssigned(ApplicationVersion applicationVersion, Long caseOfficerWuaId) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CASE_OFFICER_ASSIGNED)
        .withMainEventUserWuaId(AUDIT_USER_WUA_ID)
        .withEventDateTime(DUMMY_INSTANT)
        .withOtherEventUserWuaId(caseOfficerWuaId)
        .build();
  }
}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import java.time.Clock;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAudit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

public class ApplicationVersionAuditTestUtil {

  static final Long CASE_OFFICER_WUA_ID_1 = 1L;

  static final Long CASE_OFFICER_WUA_ID_2 = 2L;

  static final Long CAM_USER_WUA_ID_1 = 100L;

  static final Long AUDIT_USER_WUA_ID = 1000L;

  static final Long OPERATOR_WUA_ID = 1L;

  static final Instant DUMMY_INSTANT = Clock.systemDefaultZone().instant();

  static ApplicationVersionAudit getApplicationVersionAuditCaseOwnershipTaken(ApplicationVersion applicationVersion) {
    return new ApplicationVersionAudit(
        applicationVersion.getId(),
        CASE_OFFICER_WUA_ID_1,
        DUMMY_INSTANT,
        CASE_OFFICER_WUA_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        null,
        RegulatorTeamRole.CASE_OFFICER
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
        CASE_OFFICER_WUA_ID_1,
        ApplicationVersionStatus.SUBMITTED,
        null,
        null
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
        AUDIT_USER_WUA_ID,
        ApplicationVersionStatus.SUBMITTED,
        null,
        RegulatorTeamRole.CASE_OFFICER
    );
  }

  static ApplicationVersionAudit getApplicationVersionAuditCamAssigned(ApplicationVersion applicationVersion, Long caseOfficerWuaId, Long camUserWuaId) {
    return new ApplicationVersionAudit(
        applicationVersion.getId(),
        caseOfficerWuaId,
        DUMMY_INSTANT,
        caseOfficerWuaId,
        ApplicationVersionStatus.SUBMITTED,
        camUserWuaId,
        RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER
    );
  }

  static CaseEvent getCaseEventCamAssigned(ApplicationVersion applicationVersion, Long caseOfficerWuaId, Long camUserWuaId) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CAM_ASSIGNED)
        .withMainEventUserWuaId(caseOfficerWuaId)
        .withEventDateTime(DUMMY_INSTANT)
        .withOtherEventUserWuaId(camUserWuaId)
        .build();
  }

  static ApplicationVersionAudit getApplicationVersionAuditCaseOfficerReassigned(ApplicationVersion applicationVersion, Long camUserWuaId) {
    return new ApplicationVersionAudit(
        applicationVersion.getId(),
        CASE_OFFICER_WUA_ID_1,
        DUMMY_INSTANT,
        camUserWuaId,
        ApplicationVersionStatus.SUBMITTED,
        null,
        RegulatorTeamRole.CASE_OFFICER
    );
  }

  static CaseEvent getCaseEventOfficerReassigned(ApplicationVersion applicationVersion, Long camUserWuaId) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CASE_OFFICER_REASSIGNED)
        .withMainEventUserWuaId(camUserWuaId)
        .withEventDateTime(DUMMY_INSTANT)
        .withOtherEventUserWuaId(CASE_OFFICER_WUA_ID_1)
        .build();
  }

  static CaseEvent getCaseEventOfficerAssigned(ApplicationVersion applicationVersion, Long caseOfficerWuaId) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CASE_OFFICER_ASSIGNED)
        .withMainEventUserWuaId(AUDIT_USER_WUA_ID)
        .withEventDateTime(DUMMY_INSTANT)
        .withOtherEventUserWuaId(caseOfficerWuaId)
        .build();
  }

  public static ApplicationVersionAudit getApplicationVersionAuditApplicationDeleted(
      ApplicationVersion applicationVersion) {
    return new ApplicationVersionAudit(
        applicationVersion.getId(),
        OPERATOR_WUA_ID,
        DUMMY_INSTANT,
        OPERATOR_WUA_ID,
        ApplicationVersionStatus.DELETED,
        null,
        RegulatorTeamRole.CASE_OFFICER
    );
  }
}

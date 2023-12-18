package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

public record ApplicationVersionAudit(
    Integer applicationVersionId,
    Long caseOfficerWuaId,
    Instant auditDateTime,
    Long auditUserWuaId,
    ApplicationVersionStatus status,
    Long camWuaId,
    RegulatorTeamRole currentCaseOwner) {
}

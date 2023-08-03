package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;

public record ApplicationVersionAudit(
    Integer applicationVersionId,
    Long caseOfficerWuaId,
    Instant auditDateTime,
    Long auditUserWuaId) {
}

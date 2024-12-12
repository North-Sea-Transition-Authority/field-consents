package uk.co.nstauthority.fieldconsents.application;

import jakarta.annotation.Nullable;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.teams.Role;

public record ApplicationVersionAudit(
    Integer applicationVersionId,
    Long caseOfficerWuaId,
    Instant auditDateTime,
    // This value can be null as side effect from a non-user instigated change.
    // For example, when an entity is saved as part of an async background job.
    @Nullable Long auditUserWuaId,
    ApplicationVersionStatus status,
    Long camWuaId,
    Role currentCaseOwner) {
}

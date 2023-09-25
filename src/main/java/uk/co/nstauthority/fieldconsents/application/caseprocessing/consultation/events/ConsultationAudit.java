package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import java.time.Instant;
import org.hibernate.envers.RevisionType;

record ConsultationAudit(
    RevisionType revisionType,
    Integer consultationId,
    Long responderWuaId,
    Long requestedByWuaId,
    Long triggeredByWuaId,
    Instant requestDeadline,
    Instant createdDateTime
) {

}

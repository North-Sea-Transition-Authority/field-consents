package uk.co.nstauthority.fieldconsents.audit;

import org.hibernate.envers.RevisionType;

public record FieldConsentsAudit<T>(
    T entity,
    AuditRevision auditRevision,
    RevisionType revisionType
) {

}

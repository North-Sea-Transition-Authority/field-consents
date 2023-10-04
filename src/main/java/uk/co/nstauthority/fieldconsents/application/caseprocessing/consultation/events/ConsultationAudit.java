package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import org.hibernate.envers.RevisionType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;

record ConsultationAudit(Consultation consultation, AuditRevision auditRevision, RevisionType revisionType) {

}

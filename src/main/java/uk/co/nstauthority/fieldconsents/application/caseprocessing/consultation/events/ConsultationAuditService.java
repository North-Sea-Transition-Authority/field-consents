package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;

@Service
class ConsultationAuditService {

  private static final boolean SELECT_ENTITIES_ONLY = false;
  private static final boolean SELECT_DELETED_ENTITIES = false;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  ConsultationAuditService(EntityManager entityManager, TransactionTemplate transactionTemplate) {
    this.entityManager = entityManager;
    this.transactionTemplate = transactionTemplate;
  }

  @SuppressWarnings("unchecked")
  public List<ConsultationAudit> getConsultationAudits(Collection<Consultation> consultations) {
    var consultationIds = consultations.stream().map(Consultation::getId).collect(Collectors.toSet());

    List<Object[]> rawAuditProjections = transactionTemplate.execute(status -> AuditReaderFactory.get(entityManager).createQuery()
        .forRevisionsOfEntity(Consultation.class, SELECT_ENTITIES_ONLY, SELECT_DELETED_ENTITIES)
        .add(AuditEntity.id().in(consultationIds))
        .addOrder(AuditEntity.revisionProperty("createdDateTime").asc())
        .getResultList());

    if (Objects.isNull(rawAuditProjections) || rawAuditProjections.isEmpty()) {
      return Collections.emptyList();
    }

    return rawAuditProjections.stream().map(this::toConsultationAudit).toList();
  }

  private ConsultationAudit toConsultationAudit(Object[] projection) {
    return new ConsultationAudit(
        (Consultation) projection[0],
        (AuditRevision) projection[1],
        (RevisionType) projection[2]
    );
  }

}

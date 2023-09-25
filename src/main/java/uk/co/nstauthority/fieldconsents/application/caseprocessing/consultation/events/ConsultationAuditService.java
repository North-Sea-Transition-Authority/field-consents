package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
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
        .addProjection(AuditEntity.revisionType())
        .addProjection(AuditEntity.property("id"))
        .addProjection(AuditEntity.property("responderWuaId"))
        .addProjection(AuditEntity.property("requestedByWuaId"))
        .addProjection(AuditEntity.revisionProperty("userWuaId"))
        .addProjection(AuditEntity.property("requestDeadline"))
        .addProjection(AuditEntity.revisionProperty("createdDateTime"))
        .addOrder(AuditEntity.revisionProperty("createdDateTime").asc())
        .getResultList());

    if (Objects.isNull(rawAuditProjections) || rawAuditProjections.isEmpty()) {
      return Collections.emptyList();
    }

    return rawAuditProjections.stream().map(this::toConsultationAudit).toList();
  }

  private ConsultationAudit toConsultationAudit(Object[] projection) {
    return new ConsultationAudit(
        (RevisionType) projection[0],
        (Integer) projection[1],
        (Long) projection[2],
        (Long) projection[3],
        (Long) projection[4],
        (Instant) projection[5],
        ((Timestamp) projection[6]).toInstant()
    );
  }

}

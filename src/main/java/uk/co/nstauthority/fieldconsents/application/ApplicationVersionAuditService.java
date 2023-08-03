package uk.co.nstauthority.fieldconsents.application;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ApplicationVersionAuditService {

  private final EntityManager entityManager;

  private final TransactionTemplate transactionTemplate;

  public ApplicationVersionAuditService(EntityManager entityManager, TransactionTemplate transactionTemplate) {
    this.entityManager = entityManager;
    this.transactionTemplate = transactionTemplate;
  }

  public List<ApplicationVersionAudit> getApplicationVersionAudits(Map<Integer, ApplicationVersion> applicationVersionsMap) {
    var applicationVersionIds = applicationVersionsMap
        .keySet();

    // NOTE: At the moment this is getting all the application_version_aud rows but only projecting the columns needed
    //       to filter the case assignment events in the CaseAssignmentEventService.
    return transactionTemplate.execute(status -> {
      var auditReader = AuditReaderFactory.get(entityManager);
      AuditQuery updatedQuery = auditReader.createQuery()
          .forRevisionsOfEntity(ApplicationVersion.class, false, false)
          .add(AuditEntity.id().in(applicationVersionIds))
          .addProjection(AuditEntity.property("id"))
          .addProjection(AuditEntity.property("caseOfficerWuaId"))
          .addProjection(AuditEntity.revisionProperty("createdDateTime"))
          .addProjection(AuditEntity.revisionProperty("userWuaId"))
          .addOrder(AuditEntity.revisionProperty("createdDateTime").asc());

      List<Object[]> resultList = updatedQuery.getResultList();
      return resultList.stream()
          .map(objects -> new ApplicationVersionAudit(
              (Integer) objects[0],
              (Long) objects[1],
              ((Timestamp) objects[2]).toInstant(),
              (Long) objects[3]
          )).toList();
    });
  }
}

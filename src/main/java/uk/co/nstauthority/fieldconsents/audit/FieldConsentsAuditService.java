package uk.co.nstauthority.fieldconsents.audit;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class FieldConsentsAuditService {

  private static final boolean SELECT_ENTITIES_ONLY = false;
  private static final boolean SELECT_DELETED_ENTITIES = false;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  FieldConsentsAuditService(EntityManager entityManager, TransactionTemplate transactionTemplate) {
    this.entityManager = entityManager;
    this.transactionTemplate = transactionTemplate;
  }

  public <T> List<FieldConsentsAudit<T>> getAuditsFor(
      Class<T> entityClass,
      Function<T, Object> entityIdFunction,
      Collection<T> entities
  ) {
    if (entities.isEmpty()) {
      return Collections.emptyList();
    }

    var ids = entities.stream().map(entityIdFunction).collect(Collectors.toSet());
    var resultList = transactionTemplate.execute(status -> getAuditData(entityClass, ids));

    if (Objects.isNull(resultList) || resultList.isEmpty()) {
      return Collections.emptyList();
    }

    var audits = new ArrayList<FieldConsentsAudit<T>>();
    for (var rawResult : resultList) {
      audits.add(toFieldConsentsAudit(rawResult));
    }
    return audits;
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> getAuditData(Class<?> entityClass, Collection<Object> ids) {
    return AuditReaderFactory.get(entityManager).createQuery()
        .forRevisionsOfEntity(entityClass, SELECT_ENTITIES_ONLY, SELECT_DELETED_ENTITIES)
        .add(AuditEntity.id().in(ids))
        .addOrder(AuditEntity.revisionProperty("createdDateTime").asc())
        .getResultList();
  }

  @SuppressWarnings("unchecked")
  private <T> FieldConsentsAudit<T> toFieldConsentsAudit(Object[] rawResult) {
    return new FieldConsentsAudit<>((T) rawResult[0], (AuditRevision) rawResult[1], (RevisionType) rawResult[2]);
  }

}

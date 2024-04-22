package uk.co.nstauthority.fieldconsents.audit;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class FieldConsentsAuditService {

  private static final boolean SELECT_ENTITIES_ONLY = false;

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
    var lookupPropertyValues = entities
        .stream()
        .map(entityIdFunction)
        .toList();

    return getAuditsFor(entityClass, entityIdFunction, lookupPropertyValues, "id");
  }

  public <T> List<FieldConsentsAudit<T>> getAuditsFor(
      Class<T> entityClass,
      Function<T, Object> entityIdFunction,
      Collection<?> lookupPropertyValues,
      String lookupProperty
  ) {
    if (lookupPropertyValues.isEmpty()) {
      return Collections.emptyList();
    }

    var resultList = transactionTemplate.execute(status ->
        getAuditData(entityClass, entityIdFunction, lookupPropertyValues, lookupProperty));

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
  private <T> List<Object[]> getAuditData(Class<T> entityClass,
                                          Function<T, Object> entityIdFunction,
                                          Collection<?> lookupPropertyValues,
                                          String lookupProperty) {
    List<Object[]> results = AuditReaderFactory.get(entityManager).createQuery()
        .forRevisionsOfEntity(entityClass, SELECT_ENTITIES_ONLY, false)
        .add(AuditEntity.property(lookupProperty).in(lookupPropertyValues))
        .getResultList();

    if (Objects.isNull(results) || results.isEmpty()) {
      return Collections.emptyList();
    }

    var resultIds = results.stream()
        .map(result -> entityIdFunction.apply((T) result[0])).toList();

    List<Object[]> deletedResults = AuditReaderFactory.get(entityManager).createQuery()
        .forRevisionsOfEntity(entityClass, SELECT_ENTITIES_ONLY, true)
        .add(AuditEntity.id().in(resultIds))
        .add(AuditEntity.revisionType().eq(RevisionType.DEL))
        .getResultList();

    return Stream.concat(results.stream(), deletedResults.stream())
        .sorted(Comparator.comparing(result -> ((AuditRevision) result[1]).getCreatedDateTime()))
        .toList();
  }

  @SuppressWarnings("unchecked")
  private <T> FieldConsentsAudit<T> toFieldConsentsAudit(Object[] rawResult) {
    return new FieldConsentsAudit<>((T) rawResult[0], (AuditRevision) rawResult[1], (RevisionType) rawResult[2]);
  }

}

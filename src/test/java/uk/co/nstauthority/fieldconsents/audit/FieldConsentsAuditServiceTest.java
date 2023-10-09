package uk.co.nstauthority.fieldconsents.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.order.AuditOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNote;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;

@ExtendWith(MockitoExtension.class)
class FieldConsentsAuditServiceTest {

  private static final MockedStatic<AuditReaderFactory> AUDIT_READER_FACTORY_MOCKED_STATIC = mockStatic(AuditReaderFactory.class, "AuditReaderFactory");

  @Mock
  private EntityManager entityManager;

  @Mock
  private TransactionTemplate transactionTemplate;

  @Mock
  private AuditReader auditReader;

  @Mock
  private AuditQuery auditQuery;

  @Mock
  private AuditQueryCreator auditQueryCreator;

  @InjectMocks
  private FieldConsentsAuditService fieldConsentsAuditService;

  @Captor
  private ArgumentCaptor<AuditCriterion> auditCriterionArgumentCaptor;

  @Captor
  private ArgumentCaptor<AuditOrder> auditOrderArgumentCaptor;

  @AfterAll
  public static void tearDown() {
    AUDIT_READER_FACTORY_MOCKED_STATIC.close();
  }

  @ParameterizedTest
  @MethodSource("getAuditsFor_arguments")
  <T> void getAuditsFor_noEntitiesProvided(Class<T> entityClass, Function<T, Object> idFunction) {
    assertThat(fieldConsentsAuditService.getAuditsFor(entityClass, idFunction, Collections.emptyList())).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("getAuditsFor_arguments")
  <T> void getAuditsFor(Class<T> entityClass, Function<T, Object> idFunction) {
    setUpEnversMocks();

    var id = 1;
    var entity = mock(entityClass);
    when(idFunction.apply(entity)).thenReturn(id);

    var entities = Collections.singletonList(entity);
    var audit = new FieldConsentsAudit<>(entity, mock(AuditRevision.class), RevisionType.ADD);
    var rawAuditProjection = new Object[]{audit.entity(), audit.auditRevision(), audit.revisionType()};

    when(auditQuery.getResultList()).thenReturn(Collections.singletonList(rawAuditProjection));

    assertThat(fieldConsentsAuditService.getAuditsFor(entityClass, idFunction, entities)).containsExactly(audit);
    verifyAuditQueryCreation(Collections.singletonList(1));
  }

  @ParameterizedTest
  @MethodSource("getAuditsFor_arguments")
  <T> void getAuditsFor_nullResultList(Class<T> entityClass, Function<T, Object> idFunction) {
    setUpEnversMocks();

    var id = 1;
    var entity = mock(entityClass);
    when(idFunction.apply(entity)).thenReturn(id);

    var entities = Collections.singletonList(entity);

    when(auditQuery.getResultList()).thenReturn(null);

    assertThat(fieldConsentsAuditService.getAuditsFor(entityClass, idFunction, entities)).isEmpty();
    verifyAuditQueryCreation(Collections.singletonList(1));
  }

  @ParameterizedTest
  @MethodSource("getAuditsFor_arguments")
  <T> void getAuditsFor_emptyList(Class<T> entityClass, Function<T, Object> idFunction) {
    setUpEnversMocks();

    var id = 1;
    var entity = mock(entityClass);
    when(idFunction.apply(entity)).thenReturn(id);

    var entities = Collections.singletonList(entity);

    when(auditQuery.getResultList()).thenReturn(Collections.emptyList());

    assertThat(fieldConsentsAuditService.getAuditsFor(entityClass, idFunction, entities)).isEmpty();
    verifyAuditQueryCreation(Collections.singletonList(1));
  }

  private static Stream<Arguments> getAuditsFor_arguments() {
    return Stream.of(
        arguments(
            ApplicationVersion.class,
            (Function<ApplicationVersion, Object>) ApplicationVersion::getId
        ),
        arguments(
            Consultation.class,
            (Function<Consultation, Object>) Consultation::getId
        ),
        arguments(
            CaseNote.class,
            (Function<CaseNote, Object>) CaseNote::getId
        )
    );
  }

  private void verifyAuditQueryCreation(List<Object> ids) {
    verify(auditQuery).add(auditCriterionArgumentCaptor.capture());
    verify(auditQuery).addOrder(auditOrderArgumentCaptor.capture());

    assertThat(auditCriterionArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(AuditEntity.id().in(ids));

    assertThat(auditOrderArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(AuditEntity.revisionProperty("createdDateTime").asc());
  }

  private void setUpEnversMocks() {
    when(AuditReaderFactory.get(entityManager)).thenReturn(auditReader);
    when(auditReader.createQuery()).thenReturn(auditQueryCreator);
    when(auditQueryCreator.forRevisionsOfEntity(any(Class.class), eq(false), eq(false))).thenReturn(auditQuery);
    when(auditQuery.add(any())).thenReturn(auditQuery);
    when(auditQuery.addOrder(any())).thenReturn(auditQuery);

    var transactionStatus = mock(TransactionStatus.class);
    doAnswer(invocation -> invocation.getArgument(0, TransactionCallback.class).doInTransaction(transactionStatus))
        .when(transactionTemplate)
        .execute(any());
  }

}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.order.AuditOrder;
import org.hibernate.envers.query.projection.AuditProjection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;

@ExtendWith(MockitoExtension.class)
class ConsultationAuditServiceTest {

  private static final Instant REQUEST_DEADLINE = Instant.now().plus(14, ChronoUnit.DAYS);
  private static final Instant AUDIT_TIMESTAMP = Instant.now();

  private static final ConsultationAudit CONSULTATION_AUDIT = new ConsultationAudit(
      RevisionType.ADD,
      1,
      2L,
      3L,
      4L,
      REQUEST_DEADLINE,
      AUDIT_TIMESTAMP
  );

  private static final Object[] RAW_CONSULTATION_AUDIT = new Object[]{
      CONSULTATION_AUDIT.revisionType(),
      CONSULTATION_AUDIT.consultationId(),
      CONSULTATION_AUDIT.responderWuaId(),
      CONSULTATION_AUDIT.requestedByWuaId(),
      CONSULTATION_AUDIT.triggeredByWuaId(),
      CONSULTATION_AUDIT.requestDeadline(),
      Timestamp.from(CONSULTATION_AUDIT.createdDateTime())
  };

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
  private ConsultationAuditService consultationAuditService;

  @Captor
  private ArgumentCaptor<AuditCriterion> auditCriterionArgumentCaptor;

  @Captor
  private ArgumentCaptor<AuditProjection> auditProjectionArgumentCaptor;

  @Captor
  private ArgumentCaptor<AuditOrder> auditOrderArgumentCaptor;

  private MockedStatic<AuditReaderFactory> mockedStaticAuditReaderFactory;

  private List<Consultation> consultations;

  @BeforeEach
  void setUp() {
    consultations = new ArrayList<>();
    for (var i = 0; i < 5; i++) {
      var consultation = new Consultation();
      consultation.setId(i);
      consultations.add(consultation);
    }

    mockedStaticAuditReaderFactory = mockStatic(AuditReaderFactory.class, "AuditReaderFactory");
    when(AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

    when(auditReader.createQuery()).thenReturn(auditQueryCreator);
    when(auditQueryCreator.forRevisionsOfEntity(Consultation.class, false, false)).thenReturn(auditQuery);
    when(auditQuery.addProjection(any())).thenReturn(auditQuery);
    when(auditQuery.add(any())).thenReturn(auditQuery);
    when(auditQuery.addOrder(any())).thenReturn(auditQuery);

    var transactionStatus = mock(TransactionStatus.class);
    doAnswer(invocation -> invocation.getArgument(0, TransactionCallback.class).doInTransaction(transactionStatus))
        .when(transactionTemplate)
        .execute(any());
  }

  @AfterEach
  void tearDown() {
    mockedStaticAuditReaderFactory.close();
  }

  @Test
  void getConsultationAudits() {
    when(auditQuery.getResultList()).thenReturn(Collections.singletonList(RAW_CONSULTATION_AUDIT));
    assertThat(consultationAuditService.getConsultationAudits(consultations)).containsExactly(CONSULTATION_AUDIT);
    verifyAuditQueryCreation();
  }

  @Test
  void getConsultationAudits_nullResultList() {
    when(auditQuery.getResultList()).thenReturn(null);
    assertThat(consultationAuditService.getConsultationAudits(consultations)).isEmpty();
    verifyAuditQueryCreation();
  }

  @Test
  void getConsultationAudits_emptyList() {
    when(auditQuery.getResultList()).thenReturn(Collections.emptyList());
    assertThat(consultationAuditService.getConsultationAudits(consultations)).isEmpty();
    verifyAuditQueryCreation();
  }

  private void verifyAuditQueryCreation() {
    verify(auditQuery).add(auditCriterionArgumentCaptor.capture());
    verify(auditQuery, times(7)).addProjection(auditProjectionArgumentCaptor.capture());
    verify(auditQuery).addOrder(auditOrderArgumentCaptor.capture());

    var consultationIds = consultations.stream().map(Consultation::getId).toList();
    assertThat(auditCriterionArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(AuditEntity.id().in(consultationIds));

    assertThat(auditProjectionArgumentCaptor.getAllValues())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            AuditEntity.revisionType(),
            AuditEntity.property("id"),
            AuditEntity.property("responderWuaId"),
            AuditEntity.property("requestedByWuaId"),
            AuditEntity.property("requestDeadline"),
            AuditEntity.revisionProperty("createdDateTime"),
            AuditEntity.revisionProperty("userWuaId")
        );

    assertThat(auditOrderArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(AuditEntity.revisionProperty("createdDateTime").asc());
  }

}

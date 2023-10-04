package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
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
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;

@ExtendWith(MockitoExtension.class)
class ConsultationAuditServiceTest {

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
    var audit = new ConsultationAudit(mock(Consultation.class), mock(AuditRevision.class), RevisionType.ADD);
    var rawAuditProjection = new Object[]{audit.consultation(), audit.auditRevision(), audit.revisionType()};

    when(auditQuery.getResultList()).thenReturn(Collections.singletonList(rawAuditProjection));

    assertThat(consultationAuditService.getConsultationAudits(consultations)).containsExactly(audit);
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
    verify(auditQuery).addOrder(auditOrderArgumentCaptor.capture());

    var consultationIds = consultations.stream().map(Consultation::getId).toList();
    assertThat(auditCriterionArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(AuditEntity.id().in(consultationIds));

    assertThat(auditOrderArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(AuditEntity.revisionProperty("createdDateTime").asc());
  }

}

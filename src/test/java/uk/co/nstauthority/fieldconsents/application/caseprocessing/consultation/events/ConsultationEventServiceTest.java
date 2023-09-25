package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class ConsultationEventServiceTest {

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final Instant REVISION_TIMESTAMP = CLOCK.instant();
  private static final Instant REQUEST_DEADLINE = CLOCK.instant().plus(5, ChronoUnit.DAYS);

  @Mock
  private ConsultationService consultationService;

  @Mock
  private ConsultationAuditService consultationAuditService;

  @InjectMocks
  private ConsultationEventService consultationEventService;

  private Application application;

  private Consultation consultation;

  @Captor
  private ArgumentCaptor<Collection<Consultation>> consultationCollectionCaptor;

  @BeforeEach
  void setUp() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    consultation = new Consultation();
    consultation.setRequestApplicationVersion(applicationVersion);
    consultation.setId(1);
  }

  @Test
  void getCaseEvents_consultationRequested() {
    var consultationAudit = new ConsultationAudit(RevisionType.ADD, 1, null, 3L, 4L, REQUEST_DEADLINE, REVISION_TIMESTAMP);

    var consultations = Collections.singletonList(consultation);
    when(consultationService.getConsultationsByApplication(application)).thenReturn(consultations);
    when(consultationAuditService.getConsultationAudits(consultationCollectionCaptor.capture())).thenReturn(Collections.singletonList(consultationAudit));

    assertThat(consultationEventService.getCaseEvents(application))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            tuple(CaseEventType.CONSULTATION_REQUESTED, REVISION_TIMESTAMP, 4L, null, DateUtils.format(REQUEST_DEADLINE, DateUtils.DATE_TIME))
        );

    assertThat(consultationCollectionCaptor.getValue()).containsExactlyInAnyOrderElementsOf(consultations);
  }

  @Test
  void getCaseEvents_consultationAssigned() {
    var consultationAudit = new ConsultationAudit(RevisionType.MOD, 1, 2L, 3L, 4L, REQUEST_DEADLINE, REVISION_TIMESTAMP);

    var consultations = Collections.singletonList(consultation);
    when(consultationService.getConsultationsByApplication(application)).thenReturn(consultations);
    when(consultationAuditService.getConsultationAudits(consultationCollectionCaptor.capture())).thenReturn(Collections.singletonList(consultationAudit));

    assertThat(consultationEventService.getCaseEvents(application))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            tuple(CaseEventType.CONSULTATION_ASSIGNED, REVISION_TIMESTAMP, 4L, 2L, null)
        );

    assertThat(consultationCollectionCaptor.getValue()).containsExactlyInAnyOrderElementsOf(consultations);
  }

  @Test
  void getCaseEvents_consultationReAssigned() {
    var consultations = Collections.singletonList(consultation);

    when(consultationService.getConsultationsByApplication(application)).thenReturn(consultations);
    when(consultationAuditService.getConsultationAudits(consultationCollectionCaptor.capture())).thenReturn(List.of(
        new ConsultationAudit(RevisionType.MOD, 1, 2L, 3L, 4L, REQUEST_DEADLINE, REVISION_TIMESTAMP),
        new ConsultationAudit(RevisionType.MOD, 1, 99L, 3L, 4L, REQUEST_DEADLINE, REVISION_TIMESTAMP)
    ));

    assertThat(consultationEventService.getCaseEvents(application))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            tuple(CaseEventType.CONSULTATION_ASSIGNED, REVISION_TIMESTAMP, 4L, 2L, null),
            tuple(CaseEventType.CONSULTATION_REASSIGNED, REVISION_TIMESTAMP, 4L, 99L, null)
        );

    assertThat(consultationCollectionCaptor.getValue()).containsExactlyInAnyOrderElementsOf(consultations);
  }

  @Test
  void getCaseEvents_noConsultations() {
    when(consultationService.getConsultationsByApplication(application)).thenReturn(Collections.emptyList());
    when(consultationAuditService.getConsultationAudits(consultationCollectionCaptor.capture())).thenReturn(Collections.emptyList());

    assertThat(consultationEventService.getCaseEvents(application)).isEmpty();;
    assertThat(consultationCollectionCaptor.getValue()).isEmpty();
  }

}

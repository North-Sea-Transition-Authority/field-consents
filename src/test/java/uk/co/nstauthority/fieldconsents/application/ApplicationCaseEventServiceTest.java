package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.ApplicationVersionAuditTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;

@ExtendWith(MockitoExtension.class)
class ApplicationCaseEventServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationVersionAuditService applicationVersionAuditService;

  @Mock
  private ApplicationPaymentService applicationPaymentService;

  @InjectMocks
  private ApplicationCaseEventService applicationCaseEventService;

  private ApplicationVersion applicationVersion;

  private ApplicationVersion applicationVersionAutoSubmitted;

  private ApplicationVersion applicationVersionUpdate;

  private PaymentDto paymentDto;

  private CaseEvent applicationCreatedEvent;

  private CaseEvent paymentCompletedEvent;

  private CaseEvent applicationSubmittedEvent;

  private CaseEvent applicationAutomaticallySubmittedEvent;

  private CaseEvent applicationUpdateStartedEvent;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);

    applicationVersionAutoSubmitted = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    applicationVersionAutoSubmitted.setCreatedDateTime(applicationVersion.getCreatedDateTime());
    applicationVersionAutoSubmitted.setAutoSubmittedByWuaId(7L);

    applicationVersionUpdate = ApplicationTestUtil.getSubmittedApplicationVersionWithTypeIdAndVersionNumber(
        ApplicationType.FLARE, 2, 2
    );

    paymentDto = mock(PaymentDto.class);
    when(paymentDto.createdByUserId()).thenReturn("1");
    when(paymentDto.govUkPayCaptureSubmitInstant()).thenReturn(Instant.now());
    when(paymentDto.amountPence()).thenReturn(118000);

    applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(applicationVersion);
    paymentCompletedEvent = CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersion, paymentDto);
    applicationSubmittedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationSubmitted(applicationVersion);
    applicationAutomaticallySubmittedEvent =
        CaseHistoryEventTestUtil.getCaseEventForApplicationAutomaticallySubmitted(applicationVersionAutoSubmitted);
    applicationUpdateStartedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationUpdateStarted(applicationVersionUpdate);

    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(Collections.emptyList());
  }

  @ParameterizedTest
  @EnumSource(value = PaymentStatus.class, names = "SUCCESS", mode = EnumSource.Mode.EXCLUDE)
  void getCaseEvents_whenPaymentHasStatusOtherThanSuccess(PaymentStatus otherPaymentStatus) {
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersion));
    when(applicationPaymentService.getPaymentDtos(applicationVersion)).thenReturn(List.of(paymentDto));
    when(paymentDto.status()).thenReturn(otherPaymentStatus);

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationSubmittedEvent
        );
  }

  @Test
  void getCaseEvents_whenFirstApplicationSubmitted() {
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersion));
    when(applicationPaymentService.getPaymentDtos(applicationVersion)).thenReturn(List.of(paymentDto));
    when(paymentDto.status()).thenReturn(PaymentStatus.SUCCESS);

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            paymentCompletedEvent,
            applicationSubmittedEvent
        );
  }

  @Test
  void getCaseEvents_whenFirstApplicationRegulatorAutoSubmitted() {
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersionAutoSubmitted.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersionAutoSubmitted));

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersionAutoSubmitted.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationAutomaticallySubmittedEvent
        );
  }

  @Test
  void getCaseEvents_withApplicationUpdateStarted() {
    when(
        applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(List.of(
                applicationVersion,
                applicationVersionUpdate
            )
        );
    when(applicationPaymentService.getPaymentDtos(applicationVersion)).thenReturn(List.of(paymentDto));
    when(paymentDto.status()).thenReturn(PaymentStatus.SUCCESS);

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            paymentCompletedEvent,
            applicationSubmittedEvent,
            applicationUpdateStartedEvent
        );
  }

  @Test
  void getCaseEvents_whenFirstApplicationDeleted() {
    var deletedApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    deletedApplicationVersion.setStatus(ApplicationVersionStatus.DELETED);
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(deletedApplicationVersion.getApplication().getId()))
        .thenReturn(Collections.singletonList(deletedApplicationVersion));

    var applicationDeletedAudit = ApplicationVersionAuditTestUtil.getApplicationVersionAuditApplicationDeleted(deletedApplicationVersion);
    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(List.of(applicationDeletedAudit));

    var applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(deletedApplicationVersion);
    var applicationDeletedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationDeleted(deletedApplicationVersion, applicationDeletedAudit.auditDateTime());

    var caseEvents = applicationCaseEventService.getCaseEvents(deletedApplicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationDeletedEvent
        );
  }

  @Test
  void getCaseEvents_withApplicationUpdateDeleted() {
    applicationVersionUpdate.setStatus(ApplicationVersionStatus.DELETED);
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersionUpdate.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersionUpdate));
    when(
        applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(List.of(
                applicationVersion,
                applicationVersionUpdate
            )
        );
    when(applicationPaymentService.getPaymentDtos(applicationVersion)).thenReturn(List.of(paymentDto));
    when(paymentDto.status()).thenReturn(PaymentStatus.SUCCESS);

    var applicationDeletedAudit = ApplicationVersionAuditTestUtil.getApplicationVersionAuditApplicationDeleted(applicationVersionUpdate);
    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(List.of(applicationDeletedAudit));
    var applicationDeletedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationUpdateDeleted(applicationVersionUpdate, applicationDeletedAudit.auditDateTime());

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            paymentCompletedEvent,
            applicationSubmittedEvent,
            applicationUpdateStartedEvent,
            applicationDeletedEvent
        );
  }
}

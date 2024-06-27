package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;
import uk.co.nstauthority.fieldconsents.application.payment.PaymentDtoTestUtil;

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

  private PaymentDto successfulPaymentDto1;
  private PaymentDto successfulPaymentDto2;

  private CaseEvent applicationCreatedEvent;

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

    successfulPaymentDto1 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId("1")
        .withGovUkPayCaptureSubmitInstant(Instant.now())
        .withAmountPence(118000)
        .withStatus(PaymentStatus.SUCCESS)
        .build();
    successfulPaymentDto2 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId("2")
        .withGovUkPayCaptureSubmitInstant(Instant.now())
        .withAmountPence(93000)
        .withStatus(PaymentStatus.SUCCESS)
        .build();

    applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(applicationVersion);
    applicationSubmittedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationSubmitted(applicationVersion);
    applicationAutomaticallySubmittedEvent =
        CaseHistoryEventTestUtil.getCaseEventForApplicationAutomaticallySubmitted(applicationVersionAutoSubmitted);
    applicationUpdateStartedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationUpdateStarted(applicationVersionUpdate);
  }

  @ParameterizedTest
  @EnumSource(value = PaymentStatus.class, names = "SUCCESS", mode = EnumSource.Mode.EXCLUDE)
  void getCaseEvents_whenPaymentHasStatusOtherThanSuccess(PaymentStatus otherPaymentStatus) {
    var applicationVersions = Collections.singletonList(applicationVersion);

    var paymentDto = PaymentDtoTestUtil.builder()
        .withCreatedByUserId("1")
        .withGovUkPayCaptureSubmitInstant(Instant.now())
        .withAmountPence(118000)
        .withStatus(otherPaymentStatus)
        .build();

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersions);
    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(List.of(paymentDto));

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationSubmittedEvent
        );
  }

  @Test
  void getCaseEvents_whenFirstApplicationSubmitted() {
    var applicationVersions = Collections.singletonList(applicationVersion);

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersions);
    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(List.of(successfulPaymentDto1));
    when(applicationPaymentService.getApplicationVersionIdFromPaymentDto(successfulPaymentDto1)).thenReturn(applicationVersion.getId());

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersion, successfulPaymentDto1),
            applicationSubmittedEvent
        );
  }

  @Test
  void getCaseEvents_whenFirstApplicationSubmittedAndMultipleSuccessfulPayments() {
    var applicationVersions = Collections.singletonList(applicationVersion);

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersions);
    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(List.of(successfulPaymentDto1,
        successfulPaymentDto2));
    when(applicationPaymentService.getApplicationVersionIdFromPaymentDto(successfulPaymentDto1)).thenReturn(applicationVersion.getId());
    when(applicationPaymentService.getApplicationVersionIdFromPaymentDto(successfulPaymentDto2)).thenReturn(applicationVersion.getId());

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersion, successfulPaymentDto1),
            CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersion, successfulPaymentDto2),
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
    var applicationVersions = List.of(applicationVersion, applicationVersionUpdate);

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersions);
    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(List.of(successfulPaymentDto1));
    when(applicationPaymentService.getApplicationVersionIdFromPaymentDto(successfulPaymentDto1)).thenReturn(applicationVersion.getId());

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersion, successfulPaymentDto1),
            applicationSubmittedEvent,
            applicationUpdateStartedEvent
        );
  }

  @Test
  void getCaseEvents_withApplicationUpdateStartedAndPaidFor() {
    var applicationVersions = List.of(applicationVersion, applicationVersionUpdate);

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersions);
    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(List.of(successfulPaymentDto1,
        successfulPaymentDto2));
    when(applicationPaymentService.getApplicationVersionIdFromPaymentDto(successfulPaymentDto1)).thenReturn(applicationVersion.getId());
    when(applicationPaymentService.getApplicationVersionIdFromPaymentDto(successfulPaymentDto2)).thenReturn(applicationVersionUpdate.getId());

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersion, successfulPaymentDto1),
            applicationSubmittedEvent,
            applicationUpdateStartedEvent,
            CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersionUpdate, successfulPaymentDto2)
        );
  }

  @Test
  void getCaseEvents_whenNoAuditDataForApplicationDeleted() {
    var deletedApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    deletedApplicationVersion.setStatus(ApplicationVersionStatus.DELETED);
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(deletedApplicationVersion.getApplication().getId()))
        .thenReturn(Collections.singletonList(deletedApplicationVersion));

    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(Collections.emptyList());

    var applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(deletedApplicationVersion);
    var applicationDeletedEvent = CaseEvent
        .builder(deletedApplicationVersion)
        .withEventType(CaseEventType.APPLICATION_DELETED)
        .withEventDateTime(deletedApplicationVersion.getCreatedDateTime().plusMillis(1))
        .build();

    var caseEvents = applicationCaseEventService.getCaseEvents(deletedApplicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationDeletedEvent
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

    var applicationVersions = List.of(applicationVersion, applicationVersionUpdate);

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersionUpdate.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersionUpdate));
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(applicationVersions);
    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(List.of(successfulPaymentDto1));
    when(applicationPaymentService.getApplicationVersionIdFromPaymentDto(successfulPaymentDto1)).thenReturn(applicationVersion.getId());

    var applicationDeletedAudit = ApplicationVersionAuditTestUtil.getApplicationVersionAuditApplicationDeleted(applicationVersionUpdate);
    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(List.of(applicationDeletedAudit));
    var applicationDeletedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationUpdateDeleted(applicationVersionUpdate, applicationDeletedAudit.auditDateTime());

    var caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            CaseHistoryEventTestUtil.getCaseEventForPaymentCompleted(applicationVersion, successfulPaymentDto1),
            applicationSubmittedEvent,
            applicationUpdateStartedEvent,
            applicationDeletedEvent
        );
  }
}

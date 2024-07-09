package uk.co.nstauthority.fieldconsents.application;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_AUTOMATICALLY_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_CREATED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_DELETED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_UPDATE_STARTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.DRAFT_APPLICATION_UPDATE_DELETED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.PAYMENT_COMPLETED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;


@Service
public class ApplicationCaseEventService implements CaseEventService<Application> {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationVersionAuditService applicationVersionAuditService;
  private final ApplicationPaymentService applicationPaymentService;

  @Autowired
  ApplicationCaseEventService(
      ApplicationVersionService applicationVersionService,
      ApplicationVersionAuditService applicationVersionAuditService,
      ApplicationPaymentService applicationPaymentService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationVersionAuditService = applicationVersionAuditService;
    this.applicationPaymentService = applicationPaymentService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();

    var allApplicationVersions =
        applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId());

    // we will only get 1 audit event for each deleted application version
    Map<Integer, ApplicationVersionAudit> applicationVersionDeleteAuditsMap = applicationVersionAuditService
        .getApplicationVersionAudits(allApplicationVersions)
        .stream()
        .filter(applicationVersionAudit -> ApplicationVersionStatus.DELETED.equals(applicationVersionAudit.status()))
        .collect(Collectors.toMap(
            ApplicationVersionAudit::applicationVersionId,
            Function.identity()
        ));

    var successfulPaymentDtosByApplicationVersionId = applicationPaymentService.getPaymentDtos(allApplicationVersions)
        .stream()
        .filter(paymentDto -> paymentDto.status() == PaymentStatus.SUCCESS)
        .collect(Collectors.groupingBy(applicationPaymentService::getApplicationVersionIdFromPaymentDto));

    for (var applicationVersion : allApplicationVersions) {
      caseEvents.add(
          CaseEvent.builder(applicationVersion)
              .withEventType(applicationVersion.isUpdateVersion() ? APPLICATION_UPDATE_STARTED : APPLICATION_CREATED)
              .withMainEventUserWuaId(applicationVersion.getCreatedByWuaId())
              .withEventDateTime(applicationVersion.getCreatedDateTime())
              .build()
      );

      for (var paymentDto : successfulPaymentDtosByApplicationVersionId.getOrDefault(applicationVersion.getId(), List.of())) {
        caseEvents.add(
            CaseEvent.builder(applicationVersion)
                .withEventType(PAYMENT_COMPLETED)
                .withMainEventUserWuaId(Long.parseLong(paymentDto.createdByUserId()))
                .withEventDateTime(paymentDto.successInstant())
                .withEventText(DecimalFormatUtils.formatMoney((double) paymentDto.amountPence() / 100))
                .build()
        );
      }

      // this is only for the first version being submitted
      // application updates being submitted is catered for in ApplicationUpdateCaseEventService
      if (Objects.nonNull(applicationVersion.getSubmittedByWuaId())
          && Objects.nonNull(applicationVersion.getSubmittedDateTime())
          && applicationVersion.isFirstVersion()) {

        var autoSubmittedByWuaId = applicationVersion.getAutoSubmittedByWuaId();
        if (autoSubmittedByWuaId != null) {
          caseEvents.add(
              CaseEvent.builder(applicationVersion)
                  .withEventType(APPLICATION_AUTOMATICALLY_SUBMITTED)
                  .withMainEventUserWuaId(autoSubmittedByWuaId)
                  .withOtherEventUserWuaId(applicationVersion.getSubmittedByWuaId())
                  .withEventDateTime(applicationVersion.getSubmittedDateTime())
                  .build()
          );
        } else {
          caseEvents.add(
              CaseEvent.builder(applicationVersion)
                  .withEventType(APPLICATION_SUBMITTED)
                  .withMainEventUserWuaId(applicationVersion.getSubmittedByWuaId())
                  .withEventDateTime(applicationVersion.getSubmittedDateTime())
                  .build()
          );
        }
      }

      if (ApplicationVersionStatus.DELETED.equals(applicationVersion.getStatus())) {
        var applicationVersionDeleteAudit = applicationVersionDeleteAuditsMap.get(applicationVersion.getId());

        // migrated cases will have no audit rows, so we fall back to the base application version data

        // we don't know the user who deleted the application version for migrated cases, so we leave `null`
        var eventUserWuaId = applicationVersionDeleteAudit == null
            ? null
            : applicationVersionDeleteAudit.auditUserWuaId();

        // to ensure the deleted event is shown after the create event we add 1 millisecond to the created date time
        // NOTE: this is only for migrated cases
        var eventDateTime = applicationVersionDeleteAudit == null
            ? applicationVersion.getCreatedDateTime().plusMillis(1)
            : applicationVersionDeleteAudit.auditDateTime();

        caseEvents.add(
            CaseEvent.builder(applicationVersion)
                .withEventType(applicationVersion.isUpdateVersion() ? DRAFT_APPLICATION_UPDATE_DELETED : APPLICATION_DELETED)
                .withMainEventUserWuaId(eventUserWuaId)
                .withEventDateTime(eventDateTime)
                .build()
        );
      }
    }
    return caseEvents;
  }
}

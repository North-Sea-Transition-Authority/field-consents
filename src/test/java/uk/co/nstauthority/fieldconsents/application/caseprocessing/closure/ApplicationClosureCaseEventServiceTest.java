package uk.co.nstauthority.fieldconsents.application.caseprocessing.closure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAudit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAuditService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ExtendWith(MockitoExtension.class)
class ApplicationClosureCaseEventServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationVersionAuditService applicationVersionAuditService;

  @InjectMocks
  private ApplicationClosureCaseEventService applicationClosureCaseEventService;

  private Application application;
  private Integer applicationId;
  private List<ApplicationVersion> applicationVersions;
  private ApplicationVersionAudit applicationVersion1CreationAudit;
  private ApplicationVersionAudit applicationVersion1ClosureAudit;
  private ApplicationVersionAudit applicationVersion2CreationAudit;
  private CaseEvent caseEvent;

  @BeforeEach
  void setUp() {
    application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    applicationId = application.getId();
    ApplicationVersion applicationVersion1 = ApplicationTestUtil.getNewApplicationVersionWithIdAndType(1,
        ApplicationType.PRODUCTION);
    ApplicationVersion applicationVersion2 = ApplicationTestUtil.getNewApplicationVersionWithIdAndType(2,
        ApplicationType.PRODUCTION);
    applicationVersions = List.of(applicationVersion1, applicationVersion2);

    applicationVersion1CreationAudit = new ApplicationVersionAudit(
        applicationVersion1.getId(),
        100L,
        Instant.now(),
        100L,
        ApplicationVersionStatus.SUBMITTED,
        200L,
        Role.CASE_OFFICER
    );
    applicationVersion1ClosureAudit = new ApplicationVersionAudit(
        applicationVersion1.getId(),
        100L,
        Instant.now(),
        100L,
        ApplicationVersionStatus.CLOSED,
        200L,
        Role.CASE_OFFICER
    );
    applicationVersion2CreationAudit = new ApplicationVersionAudit(
        applicationVersion2.getId(),
        100L,
        Instant.now(),
        100L,
        ApplicationVersionStatus.SUBMITTED,
        200L,
        Role.CASE_OFFICER
    );

    caseEvent = CaseEvent.builder(applicationVersion1)
        .withEventType(CaseEventType.APPLICATION_CLOSED)
        .withMainEventUserWuaId(applicationVersion1ClosureAudit.caseOfficerWuaId())
        .withEventDateTime(applicationVersion1ClosureAudit.auditDateTime())
        .build();
  }

  @Test
  void getCaseEvents_withClosureEvent() {
    var applicationVersionAudits = List.of(
        applicationVersion1CreationAudit,
        applicationVersion1ClosureAudit,
        applicationVersion2CreationAudit
    );

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationId))
        .thenReturn(applicationVersions);
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersions))
        .thenReturn(applicationVersionAudits);

    assertThat(applicationClosureCaseEventService.getCaseEvents(application))
        .containsExactly(caseEvent);
  }

  @Test
  void getCaseEvents_withNoClosureEvent() {
    var applicationVersionAudits = List.of(
        applicationVersion1CreationAudit,
        applicationVersion2CreationAudit
    );

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationId))
        .thenReturn(applicationVersions);
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersions))
        .thenReturn(applicationVersionAudits);

    assertThat(applicationClosureCaseEventService.getCaseEvents(application))
        .isEmpty();
  }

}

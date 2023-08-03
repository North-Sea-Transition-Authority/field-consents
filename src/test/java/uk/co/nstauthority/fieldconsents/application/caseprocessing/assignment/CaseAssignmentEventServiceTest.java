package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.CASE_OFFICER_WUA_ID_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.CASE_OFFICER_WUA_ID_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.getApplicationVersionAuditCaseOfficerNotAssigned;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.getApplicationVersionAuditCaseOfficerAssigned;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.getApplicationVersionAuditCaseOwnershipTaken;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.getCaseEventOfficerAssigned;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.getCaseEventOwnershipReleased;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEventTestUtil.getCaseEventOwnershipTaken;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAuditService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentEventServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationVersionAuditService applicationVersionAuditService;

  @InjectMocks
  private CaseAssignmentEventService caseAssignmentEventService;

  private ApplicationVersion applicationVersion;

  private Application application;

  private Map<Integer, ApplicationVersion> applicationVersionMap;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    application = applicationVersion.getApplication();
    applicationVersionMap = Map.of(
        applicationVersion.getId(),
        applicationVersion
    );

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId()))
        .thenReturn(Collections.singletonList(applicationVersion));
  }

  @Test
  void getCaseEvents_caseOfficerOwnershipTaken_firstAssignmentEvent() {
    var applicationVersionAudits = List.of(getApplicationVersionAuditCaseOwnershipTaken(applicationVersion));
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion)
        );
  }

  @Test
  void getCaseEvents_caseOfficerAssigned_firstAssignmentEvent() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_1)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_1)
        );
  }

  @Test
  void getCaseEvents_caseOfficerOwnershipReleased_withPreviousAssignmentEvent() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion),
            getCaseEventOwnershipReleased(applicationVersion)
    );
  }

  @Test
  void getCaseEvents_caseOfficerOwnershipTaken_withPreviousAssignmentEvent() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion),
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion),
            getCaseEventOwnershipReleased(applicationVersion),
            getCaseEventOwnershipTaken(applicationVersion)
        );
  }

  @Test
  void getCaseEvents_caseOfficerAssigned_withPreviouslyReleasedCase() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_1)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion),
            getCaseEventOwnershipReleased(applicationVersion),
            getCaseEventOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_1)
        );
  }

  @Test
  void getCaseEvents_caseOfficerAssigned_withPreviousAssignmentEvent() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion),
            getCaseEventOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2)
        );
  }

  @Test
  void getCaseEvents_noCaseAssignmentEvent_withPreviousAssignmentEvent() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion),
            getCaseEventOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2)
        );
  }

  @Test
  void getCaseEvents_noCaseAssignmentEvent_withPreviousOwnershipTakenEvent() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion)
        );
  }

  @Test
  void getCaseEvents_noCaseAssignmentEvent_withPreviousOwnershipReleasedEvent() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion),
            getCaseEventOwnershipReleased(applicationVersion)
        );
  }

  @Test
  void getCaseEvents_checkOnlyRelevantEventsAreGenerated() {
    var applicationVersionAudits = List.of(
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion),
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOwnershipTaken(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion),
        getApplicationVersionAuditCaseOfficerNotAssigned(applicationVersion),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_1),
        getApplicationVersionAuditCaseOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_1)
    );
    when(applicationVersionAuditService.getApplicationVersionAudits(applicationVersionMap)).thenReturn(applicationVersionAudits);

    var caseEvents = caseAssignmentEventService.getCaseEvents(application);

    assertThat(caseEvents)
        .containsExactly(
            getCaseEventOwnershipTaken(applicationVersion),
            getCaseEventOwnershipReleased(applicationVersion),
            getCaseEventOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_2),
            getCaseEventOfficerAssigned(applicationVersion, CASE_OFFICER_WUA_ID_1)
        );
  }
}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamService;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class ConsultationEventServiceIntegrationTest extends AbstractIntegrationTest {

  private static final ServiceUserDetail INDUSTRY_USER = ServiceUserDetailTestUtil.Builder().withWuaId(1L).build();
  private static final ServiceUserDetail REGULATOR_USER = ServiceUserDetailTestUtil.Builder().withWuaId(2L).build();
  private static final ServiceUserDetail ALLOCATOR_USER_1 = ServiceUserDetailTestUtil.Builder().withWuaId(3L).build();
  private static final ServiceUserDetail ALLOCATOR_USER_2 = ServiceUserDetailTestUtil.Builder().withWuaId(4L).build();
  private static final ServiceUserDetail RESPONDER_USER_1 = ServiceUserDetailTestUtil.Builder().withWuaId(5L).build();
  private static final ServiceUserDetail RESPONDER_USER_2 = ServiceUserDetailTestUtil.Builder().withWuaId(6L).build();

  private static final Instant CONSULTATION_DEADLINE = Instant.now().plus(5, ChronoUnit.DAYS);

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ApplicationVersionRepository applicationVersionRepository;

  @Autowired
  private ConsultationEventService consultationEventService;

  @Autowired
  private ConsultationService consultationService;

  @MockBean
  private OpredTeamService opredTeamService;

  @MockBean
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  private Instant beforeTestRun;

  @BeforeEach
  void setUp() {
    beforeTestRun = Instant.now();
  }

  @Test
  void requestConsultation_consultationRequestedEventExists() {
    var applicationVersion = getSubmittedAndAssignedApplicationVersion();

    requestConsultation(applicationVersion);

    var caseEvents = getCaseEvents(applicationVersion);
    assertThat(caseEvents).hasSize(1);
    assertThatConsultationRequestedEventExists(caseEvents);
  }

  @Test
  void requestConsultation_assignResponder1_consultationAssignedEventExists() {
    var applicationVersion = getSubmittedAndAssignedApplicationVersion();
    var consultation = requestConsultation(applicationVersion);

    assignResponder(consultation, ALLOCATOR_USER_1, RESPONDER_USER_1);

    var caseEvents = getCaseEvents(applicationVersion);
    assertThat(caseEvents).hasSize(2);

    assertThatConsultationRequestedEventExists(caseEvents);

    assertThat(caseEvents.get(1))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CaseEventType.CONSULTATION_ASSIGNED,
            ALLOCATOR_USER_1.wuaId(),
            RESPONDER_USER_1.wuaId(),
            null
        );
  }

  @Test
  void requestConsultation_assignResponder1And2_consultationAssignedAndReassignedEventExists() {
    var applicationVersion = getSubmittedAndAssignedApplicationVersion();
    var consultation = requestConsultation(applicationVersion);

    assignResponder(consultation, ALLOCATOR_USER_1, RESPONDER_USER_1);
    assignResponder(consultation, ALLOCATOR_USER_2, RESPONDER_USER_2);

    var caseEvents = getCaseEvents(applicationVersion);
    assertThat(caseEvents).hasSize(3);

    assertThatConsultationRequestedEventExists(caseEvents);

    assertThat(caseEvents.get(1))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CaseEventType.CONSULTATION_ASSIGNED,
            ALLOCATOR_USER_1.wuaId(),
            RESPONDER_USER_1.wuaId(),
            null
        );

    assertThat(caseEvents.get(2))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CaseEventType.CONSULTATION_REASSIGNED,
            ALLOCATOR_USER_2.wuaId(),
            RESPONDER_USER_2.wuaId(),
            null
        );
  }

  @Test
  void requestConsultation_assignResponder1_respondToConsultation() {
    var applicationVersion = getSubmittedAndAssignedApplicationVersion();

    var consultation = requestConsultation(applicationVersion);
    assignResponder(consultation, ALLOCATOR_USER_1, RESPONDER_USER_1);
    respondToConsultation(applicationVersion, consultation, RESPONDER_USER_1);

    var caseEvents = getCaseEvents(applicationVersion);
    assertThat(caseEvents).hasSize(3);

    assertThatConsultationRequestedEventExists(caseEvents);

    assertThat(caseEvents.get(1))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CaseEventType.CONSULTATION_ASSIGNED,
            ALLOCATOR_USER_1.wuaId(),
            RESPONDER_USER_1.wuaId(),
            null
        );

    assertThat(caseEvents.get(2))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CaseEventType.CONSULTATION_RESPONDED,
            RESPONDER_USER_1.wuaId(),
            null,
            null
        );
  }

  private ApplicationVersion getSubmittedAndAssignedApplicationVersion() {
    SamlAuthenticationUtil.Builder()
        .withUser(INDUSTRY_USER)
        .setSecurityContext();

    var applicationVersion = applicationService.createNewApplicationForField(
        ApplicationType.FLARE,
        field1JsonWithOperatorAndLicences,
        orgUnit1Json,
        INDUSTRY_USER
    );

    applicationVersion.setCaseOfficerWuaId(REGULATOR_USER.wuaId());
    applicationVersion.setSubmittedByWuaId(INDUSTRY_USER.wuaId());
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    return applicationVersionRepository.save(applicationVersion);
  }

  private Consultation requestConsultation(ApplicationVersion applicationVersion) {
    SamlAuthenticationUtil.Builder()
        .withUser(REGULATOR_USER)
        .setSecurityContext();

    consultationService.requestConsultation(applicationVersion, CONSULTATION_DEADLINE, REGULATOR_USER);

    return consultationService.getLatestOpenConsultation(applicationVersion.getApplication());
  }

  private void assignResponder(Consultation consultation, ServiceUserDetail assigner, ServiceUserDetail responder) {
    SamlAuthenticationUtil.Builder()
        .withUser(assigner)
        .setSecurityContext();

    when(opredTeamService.isResponder(consultation.getConsultationTeam().toTeamId(), responder)).thenReturn(true);
    consultationService.assignResponderToConsultation(consultation, assigner, responder);
  }

  private void respondToConsultation(
      ApplicationVersion applicationVersion,
      Consultation consultation,
      ServiceUserDetail responder
  ) {
    SamlAuthenticationUtil.Builder()
        .withUser(responder)
        .setSecurityContext();

    when(opredTeamService.isResponder(consultation.getConsultationTeam().toTeamId(), responder)).thenReturn(true);
    consultationService.saveConsultationResponse(
        applicationVersion,
        consultation,
        responder,
        HabitatsRegsResponseType.AGREE,
        "I agree because...",
        EiaRegsResponseType.AGREE,
        "I agree because...",
        Collections.emptyList()
    );
  }

  private List<CaseEvent> getCaseEvents(ApplicationVersion applicationVersion) {
    return consultationEventService.getCaseEvents(applicationVersion.getApplication());
  }

  private void assertThatConsultationRequestedEventExists(List<CaseEvent> events) {
    assertThat(events).hasSizeGreaterThan(0);

    assertThat(events.get(0))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CaseEventType.CONSULTATION_REQUESTED,
            REGULATOR_USER.wuaId(),
            null,
            DateUtils.format(CONSULTATION_DEADLINE, DateUtils.DATE_TIME)
        );

    assertThat(events.get(0).eventDateTime())
        .isBeforeOrEqualTo(Instant.now())
        .isAfterOrEqualTo(beforeTestRun);
  }

}

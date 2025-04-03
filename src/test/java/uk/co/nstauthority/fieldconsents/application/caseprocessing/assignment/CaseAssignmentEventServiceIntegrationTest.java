package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_OWNERSHIP_RELEASED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_OWNERSHIP_TAKEN;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthForm;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@DirtiesContext
class CaseAssignmentEventServiceIntegrationTest extends AbstractIntegrationTest {

  private static final ServiceUserDetail ADMINISTRATOR_USER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(1L)
      .withForename("Administrator").withSurname("User")
      .withEmailAddress("administrator.user@field-consents.co.uk")
      .build();

  private static final ServiceUserDetail INDUSTRY_USER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(2L)
      .withForename("Industry").withSurname("User")
      .withEmailAddress("industry.user@field-consents.co.uk")
      .build();

  private static final ServiceUserDetail REGULATOR_USER_DETAIL = ServiceUserDetailTestUtil.Builder()
      .withWuaId(3L)
      .withForename("Regulator").withSurname("User")
      .withEmailAddress("regulator.user@field-consents.co.uk")
      .build();

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ApplicationSubmissionService applicationSubmissionService;

  @Autowired
  private ConsentLengthService consentLengthService;

  @Autowired
  private CaseAssignmentService caseAssignmentService;

  @Autowired
  private CaseAssignmentEventService caseAssignmentEventService;

  @MockitoBean
  private TeamQueryService teamQueryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    SamlAuthenticationUtil.Builder().withUser(INDUSTRY_USER_DETAIL).setSecurityContext();

    when(teamQueryService.userHasStaticRole(REGULATOR_USER_DETAIL, TeamType.REGULATOR, Role.CASE_OFFICER)).thenReturn(true);

    applicationVersion = applicationService.createNewApplicationForField(
        ApplicationType.FLARE,
        field1JsonWithOperatorAndLicences,
        orgUnit1Json,
        INDUSTRY_USER_DETAIL
    );

    var consentLengthForm = new ConsentLengthForm();
    consentLengthForm.setConsentLengthType(ConsentLengthType.ANNUAL);
    consentLengthForm.getAnnualConsentYear().setInteger(LocalDate.now().getYear());
    consentLengthService.saveConsentLengthDetails(applicationVersion, consentLengthForm);

    applicationSubmissionService.submitApplication(applicationVersion, INDUSTRY_USER_DETAIL);
  }

  @Test
  void caseOfficerTakesOwnershipOfApplicationVersion() {
    SamlAuthenticationUtil.Builder().withUser(REGULATOR_USER_DETAIL).setSecurityContext();

    caseAssignmentService.assignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL, REGULATOR_USER_DETAIL);

    assertThat(caseAssignmentEventService.getCaseEvents(applicationVersion.getApplication()))
        .hasSize(1)
        .first()
        .extracting(CaseEvent::applicationVersion, CaseEvent::eventType, CaseEvent::mainEventUserWuaId)
        .contains(applicationVersion, CASE_OFFICER_OWNERSHIP_TAKEN, REGULATOR_USER_DETAIL.wuaId());
  }

  @Test
  void assignCaseOfficerToApplicationVersion() {
    SamlAuthenticationUtil.Builder().withUser(ADMINISTRATOR_USER_DETAIL).setSecurityContext();

    caseAssignmentService.assignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL, ADMINISTRATOR_USER_DETAIL);

    assertThat(caseAssignmentEventService.getCaseEvents(applicationVersion.getApplication()))
        .hasSize(1)
        .first()
        .extracting(CaseEvent::applicationVersion, CaseEvent::eventType, CaseEvent::mainEventUserWuaId, CaseEvent::otherEventUserWuaId)
        .containsExactly(applicationVersion, CASE_OFFICER_ASSIGNED, ADMINISTRATOR_USER_DETAIL.wuaId(), REGULATOR_USER_DETAIL.wuaId());
  }

  @Test
  void caseOfficerTakesOwnershipThenReleasesOwnershipOfApplicationVersion() {
    SamlAuthenticationUtil.Builder().withUser(REGULATOR_USER_DETAIL).setSecurityContext();

    caseAssignmentService.assignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL, REGULATOR_USER_DETAIL);
    caseAssignmentService.unassignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL);

    assertThat(caseAssignmentEventService.getCaseEvents(applicationVersion.getApplication()))
        .extracting(CaseEvent::applicationVersion, CaseEvent::eventType, CaseEvent::mainEventUserWuaId)
        .containsExactly(
            tuple(applicationVersion, CASE_OFFICER_OWNERSHIP_TAKEN, REGULATOR_USER_DETAIL.wuaId()),
            tuple(applicationVersion, CASE_OFFICER_OWNERSHIP_RELEASED, REGULATOR_USER_DETAIL.wuaId())
        );
  }

  @Test
  void caseOfficerAssignedThenReleasesOwnershipOfApplicationVersion() {
    SamlAuthenticationUtil.Builder().withUser(ADMINISTRATOR_USER_DETAIL).setSecurityContext();
    caseAssignmentService.assignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL, ADMINISTRATOR_USER_DETAIL);

    SamlAuthenticationUtil.Builder().withUser(REGULATOR_USER_DETAIL).setSecurityContext();
    caseAssignmentService.unassignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL);

    assertThat(caseAssignmentEventService.getCaseEvents(applicationVersion.getApplication()))
        .extracting(CaseEvent::applicationVersion, CaseEvent::eventType, CaseEvent::mainEventUserWuaId, CaseEvent::otherEventUserWuaId)
        .containsExactly(
            tuple(applicationVersion, CASE_OFFICER_ASSIGNED, ADMINISTRATOR_USER_DETAIL.wuaId(), REGULATOR_USER_DETAIL.wuaId()),
            tuple(applicationVersion, CASE_OFFICER_OWNERSHIP_RELEASED, REGULATOR_USER_DETAIL.wuaId(), null)
        );
  }

  @Test
  void caseOfficerAssignedMultipleTimes() {
    // the regulator user is assigned an application
    SamlAuthenticationUtil.Builder().withUser(ADMINISTRATOR_USER_DETAIL).setSecurityContext();
    caseAssignmentService.assignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL, ADMINISTRATOR_USER_DETAIL);

    // they are assigned again
    SamlAuthenticationUtil.Builder().withUser(ADMINISTRATOR_USER_DETAIL).setSecurityContext();
    caseAssignmentService.assignCaseOfficer(applicationVersion, REGULATOR_USER_DETAIL, ADMINISTRATOR_USER_DETAIL);

    assertThat(caseAssignmentEventService.getCaseEvents(applicationVersion.getApplication()))
        .hasSize(1)
        .first()
        .extracting(CaseEvent::applicationVersion, CaseEvent::eventType, CaseEvent::mainEventUserWuaId, CaseEvent::otherEventUserWuaId)
        .containsExactly(applicationVersion, CASE_OFFICER_ASSIGNED, ADMINISTRATOR_USER_DETAIL.wuaId(), REGULATOR_USER_DETAIL.wuaId());
  }

}

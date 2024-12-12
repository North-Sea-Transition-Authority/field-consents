package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.FAILED_APPLICATIONS_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.FAILED_APPLICATIONS_MERGE_FIELD_TEXT;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.SUBJECT_TEXT_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.SUCCESSFUL_APPLICATIONS_MERGE_FIELD_TEXT;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.WORK_AREA_URL_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit3;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartner;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentEmailService;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class BulkIssueConsentEmailServiceTest {

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final String WORK_AREA_URL = "/workarea_url";
  private static final Long CASE_OFFICER_1_WUA_ID = 10L;
  private static final Long CASE_OFFICER_2_WUA_ID = 20L;
  private static final Long CAM_USER_1_WUA_ID = 30L;
  private static final Long CAM_USER_2_WUA_ID = 40L;
  private static final Integer OPERATOR_SHELL_1_ID = 10;
  private static final Integer OPERATOR_SHELL_2_ID = 20;
  private static final Long SUBMITTER_SHELL_WUA_ID = 10L;
  private static final Long CONSENT_RECIPIENT_SHELL_WUA_ID = 20L;
  private static final Long EDITOR_SHELL_WUA_ID = 30L;
  private static final Long CREATOR_SHELL_WUA_ID = 40L;
  private static final Integer OPERATOR_BP_ID = 30;
  private static final Long SUBMITTER_BP_WUA_ID = 30L;
  private static final Long CONSENT_RECIPIENT_BP_1_WUA_ID = 40L;

  private static final String FORMATTED_SUCCESSFUL_APPLICATIONS = "successful applications";
  private static final String FORMATTED_FAILED_APPLICATIONS = "failed applications";
  private static final EnergyPortalUserDto ENERGY_PORTAL_USER = EnergyPortalUserDtoTestUtil.Builder()
      .withWebUserAccountId(10L)
      .withForename("Forename")
      .withSurname("Surname")
      .withEmailAddress("forename@surname.com")
      .build();
  private static final Team INDUSTRY_TEAM_1 = TeamTestUtil.newBuilder()
      .withTeamType(TeamType.INDUSTRY)
      .build();
  private static final OrganisationGroupDto ORG_GROUP_1_DTO = OrganisationGroupDto.from(ORG_GROUP_1);
  private static final OrganisationGroupDto ORG_GROUP_2_DTO = OrganisationGroupDto.from(ORG_GROUP_2);
  private static final String PRODUCTION_CASE_REFERENCE = "PCON/8000/0";
  private static final String FLARE_CASE_REFERENCE = "FCON/8001/0";
  private static final String VENT_CASE_REFERENCE = "VCON/8002/0";

  @Mock
  private EmailService emailService;

  @Mock
  private AbsoluteUrlService absoluteUrlService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  @Mock
  private ConsentEmailService consentEmailService;

  @Mock
  private TeamQueryService teamQueryService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  @Spy
  @InjectMocks
  private BulkIssueConsentEmailService bulkIssueConsentEmailService;
  private BulkIssueConsentRun bulkIssueConsentRun;

  private ApplicationVersion productionApplicationVersion;
  private ApplicationVersion flareApplicationVersion;
  private ApplicationVersion ventApplicationVersion;

  private BulkIssueConsentsTask task1;
  private BulkIssueConsentsTask task2;
  private BulkIssueConsentsTask task3;

  private ConsentFieldEquityPartner consentFieldEquityPartner1;
  private ConsentFieldEquityPartner consentFieldEquityPartner2;
  private ConsentFieldEquityPartner consentFieldEquityPartner3;

  @BeforeEach
  void setUp() {
    productionApplicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_1_WUA_ID, CAM_USER_1_WUA_ID);
    productionApplicationVersion.setId(1);
    productionApplicationVersion.setSubmittedByWuaId(SUBMITTER_SHELL_WUA_ID);
    productionApplicationVersion.setPrimaryOperatorOuId(OPERATOR_SHELL_1_ID);

    flareApplicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.FLARE, CASE_OFFICER_2_WUA_ID, CAM_USER_2_WUA_ID);
    flareApplicationVersion.setId(2);
    flareApplicationVersion.setSubmittedByWuaId(SUBMITTER_BP_WUA_ID);
    flareApplicationVersion.setPrimaryOperatorOuId(OPERATOR_BP_ID);
    ventApplicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.VENT, CASE_OFFICER_1_WUA_ID, CAM_USER_2_WUA_ID);
    ventApplicationVersion.setPrimaryOperatorOuId(OPERATOR_BP_ID);
    ventApplicationVersion.setId(3);
    bulkIssueConsentRun = new BulkIssueConsentRun(ENERGY_PORTAL_USER.webUserAccountId());
    bulkIssueConsentRun.setId(UUID.randomUUID());

    task1 = new BulkIssueConsentsTask();
    task1.setConsent(ConsentTestUtil.newBuilder().withId(1).build());
    task1.setApplicationVersion(productionApplicationVersion);
    task1.setFinishedAt(CLOCK.instant());
    task1.setErrorDetails(null);
    task2 = new BulkIssueConsentsTask();
    task2.setConsent(ConsentTestUtil.newBuilder().withId(2).build());
    task2.setApplicationVersion(flareApplicationVersion);
    task2.setFinishedAt(CLOCK.instant());
    task2.setErrorDetails(null);
    task3 = new BulkIssueConsentsTask();
    task3.setConsent(ConsentTestUtil.newBuilder().withId(3).build());
    task3.setApplicationVersion(ventApplicationVersion);
    task3.setFinishedAt(CLOCK.instant());
    task3.setErrorDetails(null);

    consentFieldEquityPartner1 = new ConsentFieldEquityPartner(task1.getConsent(), OPERATOR_SHELL_1_ID, "org A", "1");
    consentFieldEquityPartner2 = new ConsentFieldEquityPartner(task2.getConsent(), OPERATOR_SHELL_2_ID, "org B", "2");
    consentFieldEquityPartner3 = new ConsentFieldEquityPartner(task3.getConsent(), OPERATOR_BP_ID, "org C", "3");
  }

  @Test
  void sendBulkConsentIssuedEmailToRegulators_whenTasksWithDifferentRegulators() {
    var productionApplicationVersion1 = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_1_WUA_ID, CAM_USER_1_WUA_ID);
    var productionApplicationVersion2 = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_2_WUA_ID, CAM_USER_2_WUA_ID);
    var flareApplicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.FLARE, CASE_OFFICER_1_WUA_ID, CAM_USER_1_WUA_ID);
    var flareApplicationVersion2 = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.FLARE, CASE_OFFICER_2_WUA_ID, CAM_USER_2_WUA_ID);
    var tasks = getBulkIssueConsentTasksFromApplicationVersions(
        List.of(productionApplicationVersion1, productionApplicationVersion2, flareApplicationVersion, flareApplicationVersion2));

    doNothing().when(bulkIssueConsentEmailService).sendBulkConsentIssuedEmailToRegulator(any(), any(), any()); // this is tested below

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToRegulators(bulkIssueConsentRun, tasks);

    verify(bulkIssueConsentEmailService, times(4)).sendBulkConsentIssuedEmailToRegulator(any(), any(), any());
  }

  @Test
  void sendBulkConsentIssuedEmailToRegulators_whenTasksWithSameRegulators() {
    var productionApplicationVersion1 = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_1_WUA_ID, CASE_OFFICER_1_WUA_ID);
    var productionApplicationVersion2 = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_2_WUA_ID, CASE_OFFICER_2_WUA_ID);
    var flareApplicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.FLARE, CASE_OFFICER_1_WUA_ID, CASE_OFFICER_1_WUA_ID);
    var flareApplicationVersion2 = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.FLARE, CASE_OFFICER_2_WUA_ID, CASE_OFFICER_2_WUA_ID);
    var tasks = getBulkIssueConsentTasksFromApplicationVersions(
        List.of(productionApplicationVersion1, productionApplicationVersion2, flareApplicationVersion, flareApplicationVersion2));

    doNothing().when(bulkIssueConsentEmailService).sendBulkConsentIssuedEmailToRegulator(any(), any(), any()); // this is tested below

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToRegulators(bulkIssueConsentRun, tasks);

    verify(bulkIssueConsentEmailService, times(2)).sendBulkConsentIssuedEmailToRegulator(any(), any(), any());
  }

  @Test
  void sendEmailToRegulator_withSuccessfulApplicationsOnly() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(CASE_OFFICER_1_WUA_ID.toString()))).thenReturn(
        ENERGY_PORTAL_USER);

    var successfulApplications = List.of(PRODUCTION_CASE_REFERENCE, FLARE_CASE_REFERENCE, VENT_CASE_REFERENCE);
    doReturn(successfulApplications).when(bulkIssueConsentEmailService).getSuccessfulApplications(any());
    doReturn(FORMATTED_SUCCESSFUL_APPLICATIONS).when(bulkIssueConsentEmailService).formatStringList(successfulApplications);

    doReturn(List.of()).when(bulkIssueConsentEmailService).getFailedApplications(any());
    doNothing().when(bulkIssueConsentEmailService).sendEmailToRegulator(any(), any(), any(), any()); // this is tested below

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToRegulator(bulkIssueConsentRun, WebUserAccountId.from(CASE_OFFICER_1_WUA_ID), tasks);

    verify(bulkIssueConsentEmailService)
        .sendEmailToRegulator(bulkIssueConsentRun, FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER),
            FORMATTED_SUCCESSFUL_APPLICATIONS, "");
  }

  @Test
  void sendBulkConsentIssuedEmailToRegulator_withSuccessfulAndFailedApplications() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(CASE_OFFICER_1_WUA_ID.toString()))).thenReturn(
        ENERGY_PORTAL_USER);

    var successfulApplications = List.of(PRODUCTION_CASE_REFERENCE, FLARE_CASE_REFERENCE);
    doReturn(successfulApplications).when(bulkIssueConsentEmailService).getSuccessfulApplications(any());
    doReturn(FORMATTED_SUCCESSFUL_APPLICATIONS).when(bulkIssueConsentEmailService).formatStringList(successfulApplications);

    var failedApplications = List.of(VENT_CASE_REFERENCE);
    doReturn(failedApplications).when(bulkIssueConsentEmailService).getFailedApplications(any());
    doReturn(FORMATTED_FAILED_APPLICATIONS).when(bulkIssueConsentEmailService).formatStringList(failedApplications);

    doNothing().when(bulkIssueConsentEmailService).sendEmailToRegulator(any(), any(), any(), any()); // this is tested below

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToRegulator(bulkIssueConsentRun, WebUserAccountId.from(CASE_OFFICER_1_WUA_ID), tasks);

    verify(bulkIssueConsentEmailService)
        .sendEmailToRegulator(bulkIssueConsentRun, FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER),
            FORMATTED_SUCCESSFUL_APPLICATIONS, FORMATTED_FAILED_APPLICATIONS);
  }

  @Test
  void sendEmailToRegulator_whenFailsToSendEmailToRegulator() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(CASE_OFFICER_1_WUA_ID.toString()))).thenReturn(
        ENERGY_PORTAL_USER);

    // WHEN the sendEmailToRegulator call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(bulkIssueConsentEmailService)
        .sendEmailToRegulator(any(), any(), any(), any());

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToRegulator(
            bulkIssueConsentRun,
            WebUserAccountId.from(CASE_OFFICER_1_WUA_ID),
            tasks
        )
    );
  }

  @Test
  void getSuccessfulApplications_whenNoSuccessfulApplicationsThenEmpty() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(CLOCK.instant());
      task.setErrorDetails("Error issuing the consent");
    });

    assertThat(bulkIssueConsentEmailService.getSuccessfulApplications(tasks)).isEmpty();
  }

  @Test
  void getSuccessfulApplications_whenAllSuccessfulApplicationsThenNonEmpty() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(CLOCK.instant());
      task.setErrorDetails(null);
      task.setApplicationVersion(productionApplicationVersion);
    });
    when(applicationService.getApplicationReference(productionApplicationVersion)).thenReturn(PRODUCTION_CASE_REFERENCE);

    assertThat(bulkIssueConsentEmailService.getSuccessfulApplications(tasks)).isNotEmpty();
  }

  @Test
  void getFailedApplications_whenNoFailedApplicationsThenEmpty() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(CLOCK.instant());
      task.setErrorDetails(null);
    });

    assertThat(bulkIssueConsentEmailService.getFailedApplications(tasks)).isEmpty();
  }

  @Test
  void getFailedApplications_whenAllFailedApplicationsThenNonEmpty() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(CLOCK.instant());
      task.setErrorDetails("Error issuing the consent");
      task.setApplicationVersion(productionApplicationVersion);
    });
    when(applicationService.getApplicationReference(productionApplicationVersion)).thenReturn(PRODUCTION_CASE_REFERENCE);

    assertThat(bulkIssueConsentEmailService.getFailedApplications(tasks)).isNotEmpty();
  }

  @Test
  void sendEmailToRegulator_withNoSuccessfulOrFailedApplications() {
    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_REGULATOR))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    bulkIssueConsentEmailService.sendEmailToRegulator(
        bulkIssueConsentRun,
        FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER),
        "",
        "");

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER.displayName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME, ""),
            tuple(FAILED_APPLICATIONS_MERGE_FIELD_NAME, "")
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(bulkIssueConsentRun.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(bulkIssueConsentRun.getDomainType());
  }

  @Test
  void sendEmailToRegulator_withSuccessfulAndFailedApplications() {
    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_REGULATOR))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    bulkIssueConsentEmailService.sendEmailToRegulator(
        bulkIssueConsentRun,
        FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER),
        FORMATTED_SUCCESSFUL_APPLICATIONS,
        FORMATTED_FAILED_APPLICATIONS);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER.displayName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
                SUCCESSFUL_APPLICATIONS_MERGE_FIELD_TEXT.formatted(FORMATTED_SUCCESSFUL_APPLICATIONS)),
            tuple(FAILED_APPLICATIONS_MERGE_FIELD_NAME,
                FAILED_APPLICATIONS_MERGE_FIELD_TEXT.formatted(FORMATTED_FAILED_APPLICATIONS))
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(bulkIssueConsentRun.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(bulkIssueConsentRun.getDomainType());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperators_whenUsersInDifferentRolesInMultipleOrganisationGroups_thenSendMultipleEmails() {
    var tasksByShellOperator = getBulkIssueConsentTasksFromApplicationVersions(List.of(productionApplicationVersion));
    var tasksByBpOperator = getBulkIssueConsentTasksFromApplicationVersions(List.of(flareApplicationVersion));

    doNothing().when(bulkIssueConsentEmailService).sendBulkConsentIssuedEmailToOperator(any(), any(), any()); // this is tested below

    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);

    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName(),
        List.of(ORG_GROUP_2_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_BP_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    var consentRecipientsShell = Set.of(CONSENT_RECIPIENT_SHELL_WUA_ID);
    doReturn(consentRecipientsShell).when(bulkIssueConsentEmailService)
        .getDistinctOperatorEmailRecipientWuaIds(ORG_GROUP_1_DTO);

    var consentRecipientsBp = Set.of(CONSENT_RECIPIENT_BP_1_WUA_ID);
    doReturn(consentRecipientsBp).when(bulkIssueConsentEmailService)
        .getDistinctOperatorEmailRecipientWuaIds(ORG_GROUP_2_DTO);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperators(bulkIssueConsentRun, Stream.concat(tasksByShellOperator.stream(), tasksByBpOperator.stream()).toList());

    verify(bulkIssueConsentEmailService, times(4)).sendBulkConsentIssuedEmailToOperator(any(), any(), any());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperators_whenSubmitterIsAlsoConsentRecipient_thenOnlySendOneEmailPerOperator() {
    var tasksByShellOperator = getBulkIssueConsentTasksFromApplicationVersions(List.of(productionApplicationVersion));
    var tasksByBpOperator = getBulkIssueConsentTasksFromApplicationVersions(List.of(flareApplicationVersion));

    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);

    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName(),
        List.of(ORG_GROUP_2_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_BP_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    var consentRecipientsShell = Set.of(SUBMITTER_SHELL_WUA_ID);
    doReturn(consentRecipientsShell).when(bulkIssueConsentEmailService)
        .getDistinctOperatorEmailRecipientWuaIds(ORG_GROUP_1_DTO);

    var consentRecipientsBp = Set.of(SUBMITTER_BP_WUA_ID);
    doReturn(consentRecipientsBp).when(bulkIssueConsentEmailService)
        .getDistinctOperatorEmailRecipientWuaIds(ORG_GROUP_2_DTO);

    doNothing().when(bulkIssueConsentEmailService).sendBulkConsentIssuedEmailToOperator(any(), any(), any()); // this is tested below

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperators(bulkIssueConsentRun, Stream.concat(tasksByShellOperator.stream(), tasksByBpOperator.stream()).toList());

    verify(bulkIssueConsentEmailService, times(2)).sendBulkConsentIssuedEmailToOperator(any(), any(), any());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperators_whenDifferentRolesAndOperatorsForSameOrganisation_thenSendOneEmailPerOperator() {
    flareApplicationVersion.setSubmittedByWuaId(SUBMITTER_SHELL_WUA_ID);
    flareApplicationVersion.setPrimaryOperatorOuId(OPERATOR_SHELL_2_ID);

    var tasksByShellOperator = getBulkIssueConsentTasksFromApplicationVersions(
        List.of(productionApplicationVersion, flareApplicationVersion));

    doNothing().when(bulkIssueConsentEmailService).sendBulkConsentIssuedEmailToOperator(any(), any(), any()); // this is tested below

    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);

    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_2_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    doReturn(Set.of(CONSENT_RECIPIENT_SHELL_WUA_ID, CREATOR_SHELL_WUA_ID))
        .when(bulkIssueConsentEmailService).getDistinctOperatorEmailRecipientWuaIds(ORG_GROUP_1_DTO);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperators(bulkIssueConsentRun, tasksByShellOperator);

    verify(bulkIssueConsentEmailService, times(3)).sendBulkConsentIssuedEmailToOperator(any(), any(), any());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperator_withSuccessfulApplications() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(SUBMITTER_SHELL_WUA_ID.toString()))).thenReturn(
        ENERGY_PORTAL_USER);

    var successfulApplications = List.of(PRODUCTION_CASE_REFERENCE, FLARE_CASE_REFERENCE, VENT_CASE_REFERENCE);
    doReturn(successfulApplications).when(bulkIssueConsentEmailService).getSuccessfulApplications(any());
    doReturn(FORMATTED_SUCCESSFUL_APPLICATIONS).when(bulkIssueConsentEmailService).formatStringList(successfulApplications);

    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_OPERATOR))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperator(bulkIssueConsentRun, WebUserAccountId.from(CASE_OFFICER_1_WUA_ID), tasks);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER.displayName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
                SUCCESSFUL_APPLICATIONS_MERGE_FIELD_TEXT.formatted(FORMATTED_SUCCESSFUL_APPLICATIONS))
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(bulkIssueConsentRun.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(bulkIssueConsentRun.getDomainType());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperator_withNoSuccessfulApplications() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(SUBMITTER_SHELL_WUA_ID.toString()))).thenReturn(
        ENERGY_PORTAL_USER);

    List<String> successfulApplications = List.of();
    doReturn(successfulApplications).when(bulkIssueConsentEmailService).getSuccessfulApplications(any());

    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_OPERATOR))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperator(bulkIssueConsentRun, WebUserAccountId.from(CASE_OFFICER_1_WUA_ID), tasks);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER.displayName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME, "")
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(bulkIssueConsentRun.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(bulkIssueConsentRun.getDomainType());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperator_whenFailsToSendEmail() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(SUBMITTER_SHELL_WUA_ID.toString()))).thenReturn(
        ENERGY_PORTAL_USER);

    List<String> successfulApplications = List.of();
    doReturn(successfulApplications).when(bulkIssueConsentEmailService).getSuccessfulApplications(any());

    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_OPERATOR))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    // WHEN the sendEmailToRegulator call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(emailService)
        .sendEmail(any(), any(), any());

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperator(
            bulkIssueConsentRun,
            WebUserAccountId.from(SUBMITTER_SHELL_WUA_ID),
            tasks
        )
    );
  }

  @Test
  void getDistinctOperatorEmailRecipientWuaIds_whenNoRecipientForOperator() {
    var teamScopeIds = Collections.singleton(ORG_GROUP_1_DTO.getOrganisationGroupId().toString());
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(INDUSTRY_TEAM_1)
            .withRole(Role.VIEWER)
            .build()
    );

    when(teamQueryService.getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds))
        .thenReturn(teamRoles);

    assertThat(bulkIssueConsentEmailService.getDistinctOperatorEmailRecipientWuaIds(ORG_GROUP_1_DTO)).isEmpty();
  }

  @Test
  void getDistinctOperatorEmailRecipientWuaIds_whenMultipleRecipientsForOperator() {
    var teamScopeIds = Collections.singleton(ORG_GROUP_1_DTO.getOrganisationGroupId().toString());
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withWuaId(CONSENT_RECIPIENT_SHELL_WUA_ID)
            .withRole(Role.CONSENT_RECIPIENT)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(CREATOR_SHELL_WUA_ID)
            .withRole(Role.CREATOR)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(SUBMITTER_SHELL_WUA_ID)
            .withRole(Role.SUBMITTER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(EDITOR_SHELL_WUA_ID)
            .withRole(Role.EDITOR)
            .build()
    );

    when(teamQueryService.getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds))
        .thenReturn(teamRoles);

    assertThat(bulkIssueConsentEmailService.getDistinctOperatorEmailRecipientWuaIds(ORG_GROUP_1_DTO))
        .contains(
            CONSENT_RECIPIENT_SHELL_WUA_ID,
            CREATOR_SHELL_WUA_ID,
            SUBMITTER_SHELL_WUA_ID,
            EDITOR_SHELL_WUA_ID
        );
  }

  @Test
  void sendBulkConsentIssuedEmailToFieldEquityPartners_whenDifferentFieldEquityPartnersPerOperator() {
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task1.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner1));
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task2.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner2));
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task3.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner3));

    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_SHELL_1_ID, orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);
    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_SHELL_2_ID, orgUnit2.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_2_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    var orgUnit3WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_BP_ID, orgUnit3.getName(),
        List.of(ORG_GROUP_2_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_BP_ID),
        anyString())
    ).thenReturn(orgUnit3WithGroupsJson);
    when(applicationService.getApplicationReference(task1.getApplicationVersion())).thenReturn(PRODUCTION_CASE_REFERENCE);
    when(applicationService.getApplicationReference(task2.getApplicationVersion())).thenReturn(FLARE_CASE_REFERENCE);
    when(applicationService.getApplicationReference(task3.getApplicationVersion())).thenReturn(VENT_CASE_REFERENCE);

    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_FIELD_EQUITY_PARTNER))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToFieldEquityPartners(bulkIssueConsentRun, List.of(task1, task2, task3));

    verify(consentEmailService, times(3)).sendConsentIssuedEmailToFieldEquityPartner(any(), any(), templateCaptor.capture());

    // verify mail merge fields
    var mailMergeFields = templateCaptor.getAllValues();
    assertThat(mailMergeFields).hasSize(3);

    assertThat(mailMergeFields.getFirst().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit2.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
                SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT.formatted("* %s".formatted(FLARE_CASE_REFERENCE)))
        );

    assertThat(mailMergeFields.get(1).getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit1.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
                SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT.formatted("* %s".formatted(PRODUCTION_CASE_REFERENCE)))
        );

    assertThat(mailMergeFields.get(2).getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit3.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
                SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT.formatted("* %s".formatted(VENT_CASE_REFERENCE)))
        );
  }

  @Test
  void sendBulkConsentIssuedEmailToFieldEquityPartners_whenNoSuccessfulApplications() {
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task1.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner1));
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task2.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner2));
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task3.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner3));

    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_SHELL_1_ID, orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);
    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_SHELL_2_ID, orgUnit2.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_2_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    var orgUnit3WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_BP_ID, orgUnit3.getName(),
        List.of(ORG_GROUP_2_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_BP_ID),
        anyString())
    ).thenReturn(orgUnit3WithGroupsJson);

    List<String> successfulApplications = List.of();
    doReturn(successfulApplications).when(bulkIssueConsentEmailService).getSuccessfulApplications(any());

    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_FIELD_EQUITY_PARTNER))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToFieldEquityPartners(bulkIssueConsentRun, List.of(task1, task2, task3));

    verify(consentEmailService, times(3)).sendConsentIssuedEmailToFieldEquityPartner(any(), any(), templateCaptor.capture());

    // verify mail merge fields
    var mailMergeFields = templateCaptor.getAllValues();
    assertThat(mailMergeFields).hasSize(3);

    assertThat(mailMergeFields.getFirst().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit2.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME, "")
        );

    assertThat(mailMergeFields.get(1).getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit1.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME, "")
        );

    assertThat(mailMergeFields.get(2).getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit3.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME, "")
        );
  }

  @Test
  void sendBulkConsentIssuedEmailToFieldEquityPartners_whenSameFieldEquityPartnersPerOperator() {
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task1.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner1));
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task2.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner1));
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(task3.getConsent()))
        .thenReturn(List.of(consentFieldEquityPartner3));

    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_SHELL_1_ID, orgUnit1.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);
    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(OPERATOR_BP_ID, orgUnit2.getName(),
        List.of(ORG_GROUP_1_DTO));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_BP_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    when(applicationService.getApplicationReference(task1.getApplicationVersion())).thenReturn(PRODUCTION_CASE_REFERENCE);
    when(applicationService.getApplicationReference(task2.getApplicationVersion())).thenReturn(FLARE_CASE_REFERENCE);
    when(applicationService.getApplicationReference(task3.getApplicationVersion())).thenReturn(VENT_CASE_REFERENCE);

    when(emailService.getTemplate(GovukNotifyTemplate.BULK_CONSENTS_ISSUED_TO_FIELD_EQUITY_PARTNER))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))).thenReturn(WORK_AREA_URL);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToFieldEquityPartners(bulkIssueConsentRun, List.of(task1, task2, task3));

    verify(consentEmailService, times(2)).sendConsentIssuedEmailToFieldEquityPartner(any(), any(), templateCaptor.capture());

    // verify mail merge fields
    var mailMergeFields = templateCaptor.getAllValues();
    assertThat(mailMergeFields).hasSize(2);

    assertThat(mailMergeFields.getFirst().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit1.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
                SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT.formatted("* %s".formatted(
                    String.join(System.lineSeparator() + "* ", List.of(PRODUCTION_CASE_REFERENCE, FLARE_CASE_REFERENCE)))))
        );

    assertThat(mailMergeFields.get(1).getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, orgUnit2.getName()),
            tuple(SUBJECT_TEXT_MERGE_FIELD_NAME, "Consents issued"),
            tuple(WORK_AREA_URL_MERGE_FIELD_NAME, WORK_AREA_URL),
            tuple(SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME,
                SUCCESSFUL_APPLICATIONS_FEPS_MERGE_FIELD_TEXT.formatted("* %s".formatted(VENT_CASE_REFERENCE)))
        );
  }

  @Test
  void formatStringList_whenEmptyListThenEmptyString() {
    List<String> strings = List.of();

    assertThat(bulkIssueConsentEmailService.formatStringList(strings)).isEmpty();
  }

  @Test
  void formatStringList_wheNonEmptyWithOneString() {
    List<String> strings = List.of(PRODUCTION_CASE_REFERENCE);

    assertThat(bulkIssueConsentEmailService.formatStringList(strings)).isEqualTo("* " + PRODUCTION_CASE_REFERENCE);
  }

  @Test
  void formatStringList_wheNonEmptyWithMultipleStrings() {
    List<String> strings = List.of(PRODUCTION_CASE_REFERENCE, FLARE_CASE_REFERENCE, VENT_CASE_REFERENCE);

    assertThat(bulkIssueConsentEmailService.formatStringList(strings)).isEqualTo("* " + PRODUCTION_CASE_REFERENCE +
        System.lineSeparator() + "* " + FLARE_CASE_REFERENCE +
        System.lineSeparator() + "* " + VENT_CASE_REFERENCE);
  }

  static List<BulkIssueConsentsTask> getBulkIssueConsentTasksFromApplicationVersions(List<ApplicationVersion> applicationVersions) {
    List<BulkIssueConsentsTask> tasks = new ArrayList<>();
    applicationVersions.forEach(applicationVersion -> {
      var task = new BulkIssueConsentsTask();
      task.setFinishedAt(CLOCK.instant());
      task.setApplicationVersion(applicationVersion);
      tasks.add(task);
    });
    return tasks;
  }
}
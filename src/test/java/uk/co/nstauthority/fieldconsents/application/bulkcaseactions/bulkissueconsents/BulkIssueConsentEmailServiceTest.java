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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.FAILED_APPLICATIONS_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.FAILED_APPLICATIONS_MERGE_FIELD_TEXT;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.SUBJECT_TEXT_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.SUCCESSFUL_APPLICATIONS_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.SUCCESSFUL_APPLICATIONS_MERGE_FIELD_TEXT;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentEmailService.WORK_AREA_URL_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamService;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class BulkIssueConsentEmailServiceTest {

  private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final String WORK_AREA_URL = "/workarea_url";
  private static final Long CASE_OFFICER_1_WUA_ID = 10L;
  private static final Long CASE_OFFICER_2_WUA_ID = 20L;
  private static final Long CAM_USER_1_WUA_ID = 30L;
  private static final Long CAM_USER_2_WUA_ID = 40L;
  private static final Integer OPERATOR_SHELL_1_ID = 10;
  private static final Integer OPERATOR_SHELL_2_ID = 20;
  private static final Long SUBMITTER_SHELL_WUA_ID = 10L;
  private static final Long CONSENT_RECIPIENT_SHELL_1_WUA_ID = 20L;
  private static final Long CONSENT_RECIPIENT_SHELL_2_WUA_ID = 30L;
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
  private static final Team INDUSTRY_TEAM_1 = new TeamTestUtil.TeamBuilder()
      .withId(1)
      .withTeamType(TeamType.INDUSTRY)
      .build();

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
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private IndustryTeamService industryTeamService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private BulkIssueConsentEmailService bulkIssueConsentEmailService;
  private BulkIssueConsentRun bulkIssueConsentRun;

  private ApplicationVersion productionApplicationVersion;
  private ApplicationVersion flareApplicationVersion;

  @BeforeEach
  void setUp() {
    bulkIssueConsentEmailService = spy(new BulkIssueConsentEmailService(
        emailService,
        absoluteUrlService,
        energyPortalUserService,
        applicationService,
        organisationUnitService,
        teamMemberViewService,
        industryTeamService
    ));
    productionApplicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_1_WUA_ID, CAM_USER_1_WUA_ID);
    productionApplicationVersion.setSubmittedByWuaId(SUBMITTER_SHELL_WUA_ID);
    productionApplicationVersion.setPrimaryOperatorOuId(OPERATOR_SHELL_1_ID);

    flareApplicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.FLARE, CASE_OFFICER_2_WUA_ID, CAM_USER_2_WUA_ID);
    flareApplicationVersion.setSubmittedByWuaId(SUBMITTER_BP_WUA_ID);
    flareApplicationVersion.setPrimaryOperatorOuId(OPERATOR_BP_ID);
    bulkIssueConsentRun = new BulkIssueConsentRun(ENERGY_PORTAL_USER.webUserAccountId());
    bulkIssueConsentRun.setId(UUID.randomUUID());
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

    var successfulApplications = List.of("case/ref/1", "case/ref/2", "case/ref/3", "case/ref/4", "case/ref/5");
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

    var successfulApplications = List.of("case/ref/1", "case/ref/2", "case/ref/3");
    doReturn(successfulApplications).when(bulkIssueConsentEmailService).getSuccessfulApplications(any());
    doReturn(FORMATTED_SUCCESSFUL_APPLICATIONS).when(bulkIssueConsentEmailService).formatStringList(successfulApplications);

    var failedApplications = List.of("case/ref/4", "case/ref/5");
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
      task.setFinishedAt(clock.instant());
      task.setErrorDetails("Error issuing the consent");
    });

    assertThat(bulkIssueConsentEmailService.getSuccessfulApplications(tasks)).isEmpty();
  }

  @Test
  void getSuccessfulApplications_whenAllSuccessfulApplicationsThenNonEmpty() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(clock.instant());
      task.setErrorDetails(null);
      task.setApplicationVersion(productionApplicationVersion);
    });
    when(applicationService.getApplicationReference(productionApplicationVersion)).thenReturn("PCON/8000/0");

    assertThat(bulkIssueConsentEmailService.getSuccessfulApplications(tasks)).isNotEmpty();
  }

  @Test
  void getFailedApplications_whenNoFailedApplicationsThenEmpty() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(clock.instant());
      task.setErrorDetails(null);
    });

    assertThat(bulkIssueConsentEmailService.getFailedApplications(tasks)).isEmpty();
  }

  @Test
  void getFailedApplications_whenAllFailedApplicationsThenNonEmpty() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(clock.instant());
      task.setErrorDetails("Error issuing the consent");
      task.setApplicationVersion(productionApplicationVersion);
    });
    when(applicationService.getApplicationReference(productionApplicationVersion)).thenReturn("PCON/8000/0");

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

    var orgGroup1Dto = OrganisationGroupDto.from(ORG_GROUP_1);
    var orgGroup2Dto = OrganisationGroupDto.from(ORG_GROUP_2);
    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(orgGroup1Dto));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);

    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName(),
        List.of(orgGroup2Dto));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_BP_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    var consentRecipientsShell = Set.of(CONSENT_RECIPIENT_SHELL_1_WUA_ID);
    doReturn(consentRecipientsShell).when(bulkIssueConsentEmailService)
        .getConsentRecipientWuaIds(orgGroup1Dto);

    var consentRecipientsBp = Set.of(CONSENT_RECIPIENT_BP_1_WUA_ID);
    doReturn(consentRecipientsBp).when(bulkIssueConsentEmailService)
        .getConsentRecipientWuaIds(orgGroup2Dto);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperators(bulkIssueConsentRun, Stream.concat(tasksByShellOperator.stream(), tasksByBpOperator.stream()).toList());

    verify(bulkIssueConsentEmailService, times(4)).sendBulkConsentIssuedEmailToOperator(any(), any(), any());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperators_whenSubmitterIsAlsoConsentRecipient_thenOnlySendOneEmailPerOperator() {
    var tasksByShellOperator = getBulkIssueConsentTasksFromApplicationVersions(List.of(productionApplicationVersion));
    var tasksByBpOperator = getBulkIssueConsentTasksFromApplicationVersions(List.of(flareApplicationVersion));

    var orgGroup1Dto = OrganisationGroupDto.from(ORG_GROUP_1);
    var orgGroup2Dto = OrganisationGroupDto.from(ORG_GROUP_2);
    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(orgGroup1Dto));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);

    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName(),
        List.of(orgGroup2Dto));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_BP_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    var consentRecipientsShell = Set.of(SUBMITTER_SHELL_WUA_ID);
    doReturn(consentRecipientsShell).when(bulkIssueConsentEmailService)
        .getConsentRecipientWuaIds(orgGroup1Dto);

    var consentRecipientsBp = Set.of(SUBMITTER_BP_WUA_ID);
    doReturn(consentRecipientsBp).when(bulkIssueConsentEmailService)
        .getConsentRecipientWuaIds(orgGroup2Dto);

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

    var orgGroup1Dto = OrganisationGroupDto.from(ORG_GROUP_1);
    var orgUnit1WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit1.getOrganisationUnitId(), orgUnit1.getName(),
        List.of(orgGroup1Dto));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_1_ID),
        anyString())
    ).thenReturn(orgUnit1WithGroupsJson);

    var orgUnit2WithGroupsJson = new OrganisationUnitWithGroupsJson(orgUnit2.getOrganisationUnitId(), orgUnit2.getName(),
        List.of(orgGroup1Dto));
    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(OPERATOR_SHELL_2_ID),
        anyString())
    ).thenReturn(orgUnit2WithGroupsJson);

    doReturn(Set.of(CONSENT_RECIPIENT_SHELL_1_WUA_ID, CONSENT_RECIPIENT_SHELL_2_WUA_ID))
        .when(bulkIssueConsentEmailService).getConsentRecipientWuaIds(orgGroup1Dto);

    bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperators(bulkIssueConsentRun, tasksByShellOperator);

    verify(bulkIssueConsentEmailService, times(3)).sendBulkConsentIssuedEmailToOperator(any(), any(), any());
  }

  @Test
  void sendBulkConsentIssuedEmailToOperator_withSuccessfulApplications() {
    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(SUBMITTER_SHELL_WUA_ID.toString()))).thenReturn(
        ENERGY_PORTAL_USER);

    var successfulApplications = List.of("case/ref/1", "case/ref/2", "case/ref/3", "case/ref/4", "case/ref/5");
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
  void getConsentRecipientWuaIds_whenNoConsentRecipientForOperator() {
    var orgGroupDto = OrganisationGroupDto.from(ORG_GROUP_1);

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(Collections.emptyList());

    assertThat(bulkIssueConsentEmailService.getConsentRecipientWuaIds(orgGroupDto)).isEmpty();
  }

  @Test
  void getConsentRecipientWuaIds_whenMultipleConsentRecipientsForOperator() {
    var orgGroupDto = OrganisationGroupDto.from(ORG_GROUP_1);

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    var teamMemberViewConsentRecipient1 = new TeamMemberView(
        WebUserAccountId.from(CONSENT_RECIPIENT_SHELL_1_WUA_ID),
        null,
            null,
            null,
            null,
            null,
            null,
        Set.of(IndustryTeamRole.CONSENT_RECIPIENT)
    );
    var teamMemberViewConsentRecipient2 = new TeamMemberView(
        WebUserAccountId.from(CONSENT_RECIPIENT_SHELL_2_WUA_ID),
        null,
        null,
        null,
        null,
        null,
        null,
        Set.of(IndustryTeamRole.CONSENT_RECIPIENT)
    );
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(List.of(teamMemberViewConsentRecipient1, teamMemberViewConsentRecipient2));

    assertThat(bulkIssueConsentEmailService.getConsentRecipientWuaIds(orgGroupDto))
        .containsExactly(
            CONSENT_RECIPIENT_SHELL_1_WUA_ID,
            CONSENT_RECIPIENT_SHELL_2_WUA_ID
    );
  }

  @Test
  void formatStringList_whenEmptyListThenEmptyString() {
    List<String> strings = List.of();

    assertThat(bulkIssueConsentEmailService.formatStringList(strings)).isEmpty();
  }

  @Test
  void formatStringList_wheNonEmptyWithOneString() {
    List<String> strings = List.of("case/ref/1");

    assertThat(bulkIssueConsentEmailService.formatStringList(strings)).isEqualTo("* case/ref/1");
  }

  @Test
  void formatStringList_wheNonEmptyWithMultipleStrings() {
    List<String> strings = List.of("case/ref/1", "case/ref/2", "case/ref/3");

    assertThat(bulkIssueConsentEmailService.formatStringList(strings)).isEqualTo("* case/ref/1" +
        System.lineSeparator() + "* case/ref/2" +
        System.lineSeparator() + "* case/ref/3");
  }

  static List<BulkIssueConsentsTask> getBulkIssueConsentTasksFromApplicationVersions(List<ApplicationVersion> applicationVersions) {
    List<BulkIssueConsentsTask> tasks = new ArrayList<>();
    applicationVersions.forEach(applicationVersion -> {
      var task = new BulkIssueConsentsTask();
      task.setFinishedAt(clock.instant());
      task.setApplicationVersion(applicationVersion);
      tasks.add(task);
    });
    return tasks;
  }
}
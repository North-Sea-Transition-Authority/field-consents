package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
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
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class BulkIssueConsentEmailServiceTest {

  private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final String WORK_AREA_URL = "/workarea_url";
  private static final Long CASE_OFFICER_1_WUA_ID = 10L;
  private static final Long CASE_OFFICER_2_WUA_ID = 20L;
  private static final Long CAM_USER_1_WUA_ID = 30L;
  private static final Long CAM_USER_2_WUA_ID = 40L;
  private static final String FORMATTED_SUCCESSFUL_APPLICATIONS = "successful applications";
  private static final String FORMATTED_FAILED_APPLICATIONS = "failed applications";
  private static final EnergyPortalUserDto ENERGY_PORTAL_USER = EnergyPortalUserDtoTestUtil.Builder()
      .withWebUserAccountId(10L)
      .withForename("Forename")
      .withSurname("Surname")
      .withEmailAddress("forename@surname.com")
      .build();

  @Mock
  private EmailService emailService;

  @Mock
  private AbsoluteUrlService absoluteUrlService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ApplicationService applicationService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private BulkIssueConsentEmailService bulkIssueConsentEmailService;

  private BulkIssueConsentRun bulkIssueConsentRun;

  @BeforeEach
  void setUp() {
    bulkIssueConsentEmailService = spy(new BulkIssueConsentEmailService(
        emailService,
        absoluteUrlService,
        energyPortalUserService,
        applicationService
    ));

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
    var applicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_1_WUA_ID, CAM_USER_1_WUA_ID);

    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(clock.instant());
      task.setErrorDetails(null);
      task.setApplicationVersion(applicationVersion);
    });
    when(applicationService.getApplicationReference(applicationVersion)).thenReturn("PCON/8000/0");

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
    var applicationVersion = ApplicationTestUtil.getApprovedForIssuingApplicationVersionWithType(
        ApplicationType.PRODUCTION, CASE_OFFICER_1_WUA_ID, CAM_USER_1_WUA_ID);

    var tasks = IntStream.range(0, 5).mapToObj(i -> new BulkIssueConsentsTask()).toList();
    tasks.forEach(task -> {
      task.setFinishedAt(clock.instant());
      task.setErrorDetails("Error issuing the consent");
      task.setApplicationVersion(applicationVersion);
    });
    when(applicationService.getApplicationReference(applicationVersion)).thenReturn("PCON/8000/0");

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
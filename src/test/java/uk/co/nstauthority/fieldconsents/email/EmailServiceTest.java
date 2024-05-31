package uk.co.nstauthority.fieldconsents.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.email.EmailService.SALUTATION;
import static uk.co.nstauthority.fieldconsents.email.EmailService.TEST_PREFIX;
import static uk.co.nstauthority.fieldconsents.email.EmailService.VALEDICTION;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.NotificationLibraryClient;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.TemplateType;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.branding.BrandingTestUtil;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class EmailServiceTest {

  private static final ServiceBrandingConfigurationProperties SERVICE_BRANDING_CONFIGURATION_PROPERTIES = new ServiceBrandingConfigurationProperties(
      "name",
      "mnemonic"
  );

  private static final GovukNotifyTemplate GOVUK_NOTIFY_TEMPLATE = GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER;

  private static final Template TEMPLATE = new Template(
      GOVUK_NOTIFY_TEMPLATE.getTemplateId(),
      TemplateType.EMAIL,
      Set.of(),
      Template.VerificationStatus.CONFIRMED_NOTIFY_TEMPLATE
  );

  private static ApplicationService applicationService;

  private static ApplicationAssetService applicationAssetService;

  private static ConsentLengthService consentLengthService;

  private static AbsoluteUrlService absoluteUrlService;

  private static NotificationLibraryClient notificationLibraryClient;

  private static EmailService emailService;

  private static final ApplicationVersion applicationVersion = ApplicationTestUtil
      .getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  private static ConsentLengthDetails consentDuration = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

  @BeforeAll
  static void setup() {
    applicationService = mock(ApplicationService.class);
    applicationAssetService = mock(ApplicationAssetService.class);
    consentLengthService = mock(ConsentLengthService.class);
    absoluteUrlService = mock(AbsoluteUrlService.class);
    notificationLibraryClient = mock(NotificationLibraryClient.class);

    emailService = new EmailService(
        notificationLibraryClient,
        SERVICE_BRANDING_CONFIGURATION_PROPERTIES,
        BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES,
        applicationService,
        applicationAssetService,
        consentLengthService,
        absoluteUrlService);
  }

  @DisplayName("GIVEN I want to get a template")
  @Nested
  class GetTemplateForApplication {

    @BeforeAll
    static void setup() {
      given(notificationLibraryClient.getTemplate(GOVUK_NOTIFY_TEMPLATE.getTemplateId()))
          .willReturn(TEMPLATE);
      given(applicationService.getApplicationReference(applicationVersion)).willReturn("PCON/1/0 (Version 1)");

      consentDuration = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
      given(consentLengthService.getConsentLengthDetails(applicationVersion)).willReturn(consentDuration);
      given(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationSummaryController.class)
          .getApplicationSummary(applicationVersion.getApplication().getId(), null)))).willReturn("/application-url");
      given(applicationAssetService.getPrimaryAsset(applicationVersion)).willReturn(ApplicationAssetTestUtil.fieldAsset1);
      given(applicationAssetService.getAssetJsonForApplicationAsset(ApplicationAssetTestUtil.fieldAsset1)).willReturn(FieldTestUtil.field1Json);
    }

    @DisplayName("WHEN I am in test mode")
    @Nested
    class WhenInTestMode {

      @BeforeAll
      static void setup() {
        given(notificationLibraryClient.isRunningTestMode())
            .willReturn(true);
      }

      @DisplayName("THEN a not blank test email subject prefix will be included as a mail merge field")
      @Test
      void whenInTestMode() {

        var resultingTemplate = emailService
            .getTemplateForApplication(GOVUK_NOTIFY_TEMPLATE, applicationVersion)
            .merge();

        assertThat(resultingTemplate.getMailMergeFields())
            .extracting(MailMergeField::name, MailMergeField::value)
            .containsExactlyInAnyOrder(
                tuple("SUBJECT_PREFIX", TEST_PREFIX),
                tuple("SERVICE_FULL_NAME", SERVICE_BRANDING_CONFIGURATION_PROPERTIES.name()),
                tuple("REGULATOR_MNEMONIC", BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.mnemonic()),
                tuple("APPLICATION_REFERENCE", "PCON/1/0 (Version 1)"),
                tuple("PRIMARY_ASSET", FieldTestUtil.field1Json.getName()),
                tuple("APPLICATION_DURATION", consentDuration.getConsentLength().getShortDisplayName()),
                tuple("APPLICATION_URL", "/application-url"),
                tuple("SALUTATION", SALUTATION),
                tuple("VALEDICTION", VALEDICTION),
                tuple("CONSENTS_TEAM_NAME", BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.teamName()));
      }
    }

    @DisplayName("WHEN I am in production mode")
    @Nested
    class WhenInProductionMode {

      @BeforeAll
      static void setup() {
        given(notificationLibraryClient.isRunningProductionMode())
            .willReturn(true);
      }

      @DisplayName("THEN a blank string test email subject prefix will be included as a mail merge field")
      @Test
      void whenInProductionMode() {

        var resultingTemplate = emailService
            .getTemplateForApplication(GOVUK_NOTIFY_TEMPLATE, applicationVersion)
            .merge();

        assertThat(resultingTemplate.getMailMergeFields())
            .extracting(MailMergeField::name, MailMergeField::value)
            .containsExactlyInAnyOrder(
                tuple("SUBJECT_PREFIX", ""),
                tuple("SERVICE_FULL_NAME", SERVICE_BRANDING_CONFIGURATION_PROPERTIES.name()),
                tuple("REGULATOR_MNEMONIC", BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.mnemonic()),
                tuple("APPLICATION_REFERENCE", "PCON/1/0 (Version 1)"),
                tuple("PRIMARY_ASSET", FieldTestUtil.field1Json.getName()),
                tuple("APPLICATION_DURATION", consentDuration.getConsentLength().getShortDisplayName()),
                tuple("APPLICATION_URL", "/application-url"),
                tuple("SALUTATION", SALUTATION),
                tuple("VALEDICTION", VALEDICTION),
                tuple("CONSENTS_TEAM_NAME", BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.teamName()));
      }
    }
  }

  @Test
  void sendEmail() {

    MergedTemplate mergedTemplate = MergedTemplate.builder(TEMPLATE).merge();

    CorrelationIdUtil.setCorrelationIdOnMdc("log-correlation-id");

    emailService.sendEmail(
        mergedTemplate,
        EmailRecipient.directEmailAddress("someone@example.com"),
        DomainReference.from("id", "type")
    );

    then(notificationLibraryClient)
        .should()
        .sendEmail(
            refEq(mergedTemplate),
            refEq(EmailRecipient.directEmailAddress("someone@example.com")),
            refEq(DomainReference.from("id", "type")),
            eq("log-correlation-id")
        );
  }
}

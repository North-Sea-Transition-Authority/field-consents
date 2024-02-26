package uk.co.nstauthority.fieldconsents.email;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.NotificationLibraryClient;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class EmailService {

  static final String TEST_PREFIX = "***TEST***";
  static final String SALUTATION = "Dear";
  static final String VALEDICTION = "Kind regards";

  public static final String RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME = "RECIPIENT_IDENTIFIER";
  public static final String REQUEST_DEADLINE_MERGE_FIELD_NAME = "REQUEST_DEADLINE";

  private final NotificationLibraryClient notificationLibraryClient;
  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;
  private final ApplicationService applicationService;
  private final ApplicationAssetService applicationAssetService;
  private final ConsentLengthService consentLengthService;
  private final AbsoluteUrlService absoluteUrlService;

  @Autowired
  public EmailService(NotificationLibraryClient notificationLibraryClient,
                      ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
                      CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties,
                      ApplicationService applicationService,
                      ApplicationAssetService applicationAssetService,
                      ConsentLengthService consentLengthService,
                      AbsoluteUrlService absoluteUrlService) {
    this.notificationLibraryClient = notificationLibraryClient;
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
    this.applicationService = applicationService;
    this.applicationAssetService = applicationAssetService;
    this.consentLengthService = consentLengthService;
    this.absoluteUrlService = absoluteUrlService;
  }

  public MergedTemplate.MergedTemplateBuilder getTemplate(GovukNotifyTemplate notifyTemplate,
                                                          ApplicationVersion applicationVersion) {

    var subjectPrefix = notificationLibraryClient.isRunningTestMode() ? TEST_PREFIX : "";
    var applicationReference = applicationService.getApplicationReference(applicationVersion);
    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    var primaryAssetName = applicationAssetService.getAssetJsonForApplicationAsset(primaryAsset).getName();
    var applicationUrl = absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationSummaryController.class)
        .getApplicationSummary(applicationVersion.getApplication().getId(), null)));

    return notificationLibraryClient.getTemplate(notifyTemplate.getTemplateId())
        .withMailMergeField("SUBJECT_PREFIX", subjectPrefix)
        .withMailMergeField("SERVICE_FULL_NAME", serviceBrandingConfigurationProperties.name())
        .withMailMergeField("REGULATOR_MNEMONIC", customerBrandingConfigurationProperties.mnemonic())
        .withMailMergeField("APPLICATION_REFERENCE", applicationReference)
        .withMailMergeField("PRIMARY_ASSET", primaryAssetName)
        .withMailMergeField("APPLICATION_DURATION",
            consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength().getShortDisplayName())
        .withMailMergeField("APPLICATION_URL", applicationUrl)
        .withMailMergeField("SALUTATION", SALUTATION)
        .withMailMergeField("VALEDICTION", VALEDICTION)
        .withMailMergeField("CONSENTS_TEAM_NAME", customerBrandingConfigurationProperties.teamName());
  }

  public void sendEmail(MergedTemplate mergedTemplate,
                        EmailRecipient recipient,
                        DomainReference domainReference) {
    notificationLibraryClient.sendEmail(
        mergedTemplate,
        recipient,
        domainReference,
        CorrelationIdUtil.getCorrelationIdFromMdc()
    );
  }
}

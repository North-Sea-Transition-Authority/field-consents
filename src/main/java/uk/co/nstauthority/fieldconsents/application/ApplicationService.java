package uk.co.nstauthority.fieldconsents.application;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_CREATED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.UPDATE_SUBMITTED;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

@Service
public class ApplicationService {

  static final String NOT_LATEST_APPLICATION_VERSION_ERROR_MESSAGE =
      "Cannot start update for application version id %s as this is not the latest application version id %s";

  static final String START_APPLICATION_UPDATE_ERROR_MESSAGE =
      "Cannot start update for application version id %s and status %s (status must be %s)";

  private final ApplicationRepository applicationRepository;

  private final ApplicationVersionRepository applicationVersionRepository;

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationAssetLicenceService applicationAssetLicenceService;

  private final ApplicationConfigurationProperties applicationConfigurationProperties;

  private final AceFlagService aceFlagService;

  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  private final Clock clock;

  private final ApplicationVersionService applicationVersionService;

  private final TechnicalReviewService technicalReviewService;

  public ApplicationService(ApplicationRepository applicationRepository,
                            ApplicationVersionRepository applicationVersionRepository,
                            ApplicationAssetService applicationAssetService,
                            ApplicationAssetLicenceService applicationAssetLicenceService,
                            ApplicationConfigurationProperties applicationConfigurationProperties,
                            AceFlagService aceFlagService,
                            ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
                            Clock clock,
                            ApplicationVersionService applicationVersionService,
                            TechnicalReviewService technicalReviewService) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationAssetService = applicationAssetService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
    this.applicationConfigurationProperties = applicationConfigurationProperties;
    this.aceFlagService = aceFlagService;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.clock = clock;
    this.applicationVersionService = applicationVersionService;
    this.technicalReviewService = technicalReviewService;
  }

  private ApplicationVersion createNewApplication(ApplicationType applicationType,
                                                  OrganisationUnitJson operatorOuJson,
                                                  ServiceUserDetail user) {
    Application application = createNewApplicationMasterRecord(applicationType, user);
    return createNewApplicationVersionRecord(application, operatorOuJson, user);
  }

  @Transactional
  public ApplicationVersion createNewApplicationForField(ApplicationType type,
                                                         FieldWithOperatorAndLicencesJson field,
                                                         OrganisationUnitJson operatorOuJson,
                                                         ServiceUserDetail user) {
    ApplicationVersion applicationVersion = createNewApplication(type, operatorOuJson, user);
    var applicationAsset = applicationAssetService.createPrimaryAsset(applicationVersion, field);
    applicationAssetLicenceService.createAssetLicences(applicationAsset, field);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_CREATED, INDUSTRY);
    return applicationVersion;
  }

  @Transactional
  public ApplicationVersion createNewApplicationForTerminal(ApplicationType type,
                                                            TerminalWithOperatorJson terminalWithOperatorJson,
                                                            OrganisationUnitJson operatorOuJson,
                                                            ServiceUserDetail user) {
    ApplicationVersion applicationVersion = createNewApplication(type, operatorOuJson, user);
    applicationAssetService.createPrimaryAsset(applicationVersion, terminalWithOperatorJson);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_CREATED, INDUSTRY);
    return applicationVersion;
  }

  private ApplicationVersion createNewApplicationVersionRecord(Application application,
                                                               OrganisationUnitJson operatorOuJson,
                                                               ServiceUserDetail user) {
    ApplicationVersion applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(application);
    applicationVersion.setVersion(1);
    applicationVersion.setCreatedDateTime(clock.instant());
    applicationVersion.setCreatedByWuaId(user.wuaId());
    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);
    applicationVersion.setPrimaryOperatorOuId(operatorOuJson.organisationUnitId());
    applicationVersion.setCachedPrimaryOperatorName(operatorOuJson.name());
    return applicationVersionRepository.save(applicationVersion);
  }

  @Transactional
  public void prepareApplicationForPayment(ApplicationVersion applicationVersion) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application %d cannot be prepared for payment as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    var application = applicationVersion.getApplication();

    // If the application has previously been in awaiting payment status it will already have a number assigned, so
    // don't assign a new one.
    if (application.getApplicationNo() == null) {
      application.setApplicationNo(getApplicationNumber());
    }

    applicationVersion.setStatus(ApplicationVersionStatus.AWAITING_PAYMENT);

    applicationRepository.save(application);
    applicationVersionRepository.save(applicationVersion);
  }

  @Transactional
  public void returnApplicationToInProgressFromAwaitingPayment(ApplicationVersion applicationVersion) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.AWAITING_PAYMENT.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application %d cannot be returned to in progress as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);

    applicationVersionRepository.save(applicationVersion);
  }

  @Transactional
  public void submitApplication(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.AWAITING_PAYMENT.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application %d cannot be submitted as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    var application = applicationVersion.getApplication();
    submitApplicationVersion(applicationVersion, user);
    applicationRepository.save(application);
    aceFlagService.autoSetAceFlag(applicationVersion);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_SUBMITTED, INDUSTRY);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_SUBMITTED, REGULATOR);
  }

  @Transactional
  public void submitApplicationUpdate(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application update cannot be submitted for application %d as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    submitApplicationVersion(applicationVersion, user);

    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, UPDATE_SUBMITTED, INDUSTRY);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, UPDATE_SUBMITTED, REGULATOR);
    if (technicalReviewService.openTechnicalReviewExists(applicationVersion)) {
      applicationWorkAreaPriorityService
          .prioritiseApplicationInWorkArea(applicationVersion, user, UPDATE_SUBMITTED, REGULATOR_TECHNICAL_REVIEWER);
    }
  }

  @Transactional
  public ApplicationVersion startApplicationUpdate(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var latestApplicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getApplication().getId());

    if (!applicationVersion.equals(latestApplicationVersion)) {
      throw new IllegalStateException(NOT_LATEST_APPLICATION_VERSION_ERROR_MESSAGE.formatted(
          applicationVersion.getId(),
          latestApplicationVersion.getId()));
    }

    if (!ApplicationVersionStatus.SUBMITTED.equals(applicationVersion.getStatus())) {
      throw new IllegalStateException(START_APPLICATION_UPDATE_ERROR_MESSAGE.formatted(
          applicationVersion.getId(),
          applicationVersion.getStatus().getDisplayName(),
          ApplicationVersionStatus.SUBMITTED.name()));
    }

    var newApplicationVersion = new ApplicationVersion();
    newApplicationVersion.setApplication(applicationVersion.getApplication());
    newApplicationVersion.setVersion(applicationVersion.getVersion() + 1);
    newApplicationVersion.setCreatedDateTime(clock.instant());
    newApplicationVersion.setCreatedByWuaId(user.wuaId());
    newApplicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);
    newApplicationVersion.setPrimaryOperatorOuId(applicationVersion.getPrimaryOperatorOuId());
    newApplicationVersion.setCachedPrimaryOperatorName(applicationVersion.getCachedPrimaryOperatorName());
    newApplicationVersion.setCaseOfficerWuaId(applicationVersion.getCaseOfficerWuaId());
    return applicationVersionRepository.save(newApplicationVersion);
  }

  protected void submitApplicationVersion(ApplicationVersion applicationVersion,
                                          ServiceUserDetail user) {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);
    applicationVersion.setSubmittedDateTime(clock.instant());
    applicationVersion.setSubmittedByWuaId(user.wuaId());
    applicationVersionRepository.save(applicationVersion);
  }

  public String generateApplicationReference(ApplicationVersion applicationVersion) {
    var application = applicationVersion.getApplication();
    return "%s/%d/%d (Version %d)".formatted(
        application.getType().getReferenceMnemonic(),
        application.getApplicationNo(),
        application.getVariationNo(),
        applicationVersion.getVersion());
  }

  public String getApplicationReference(ApplicationVersion applicationVersion) {
    return Objects.nonNull(applicationVersion.getApplication().getApplicationNo())
        ? generateApplicationReference(applicationVersion)
        : "";
  }

  protected int getApplicationNumber() {
    return applicationRepository.findLatestApplicationNumber()
        .map(latestApplicationNumber -> latestApplicationNumber + 1)
        .orElse(Integer.valueOf(applicationConfigurationProperties.applicationNoStartValue()));
  }

  @NotNull
  private Application createNewApplicationMasterRecord(ApplicationType applicationType,
                                                       ServiceUserDetail user) {
    Application application = new Application();
    application.setType(applicationType);
    application.setCreatedDate(clock.instant());
    application.setVariationNo(0);
    application.setCreatedByWuaId(user.wuaId());
    return applicationRepository.save(application);
  }

  public Application getApplicationById(int applicationId) {
    return applicationRepository.findById(applicationId)
        .orElseThrow(() ->
            new EntityNotFoundException("Application with id %s not found".formatted(applicationId))
        );
  }
}

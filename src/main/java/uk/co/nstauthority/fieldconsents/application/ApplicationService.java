package uk.co.nstauthority.fieldconsents.application;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_CREATED;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
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
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final Clock clock;
  private final ApplicationVersionService applicationVersionService;

  ApplicationService(
      ApplicationRepository applicationRepository,
      ApplicationVersionRepository applicationVersionRepository,
      ApplicationAssetService applicationAssetService,
      ApplicationAssetLicenceService applicationAssetLicenceService,
      ApplicationConfigurationProperties applicationConfigurationProperties,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
      Clock clock,
      ApplicationVersionService applicationVersionService
  ) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationAssetService = applicationAssetService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
    this.applicationConfigurationProperties = applicationConfigurationProperties;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.clock = clock;
    this.applicationVersionService = applicationVersionService;
  }

  @Transactional
  public ApplicationVersion createNewApplicationForField(
      ApplicationType type,
      FieldWithOperatorAndLicencesJson field,
      OrganisationUnitJson operatorOuJson,
      ServiceUserDetail user
  ) {
    var applicationVersion = createNewApplication(type, operatorOuJson, user);
    var applicationAsset = applicationAssetService.createPrimaryAsset(applicationVersion, field);
    applicationAssetLicenceService.createAssetLicences(applicationAsset, field);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_CREATED, INDUSTRY);
    return applicationVersion;
  }

  @Transactional
  public ApplicationVersion createNewApplicationForTerminal(
      ApplicationType type,
      TerminalWithOperatorJson terminalWithOperatorJson,
      OrganisationUnitJson operatorOuJson,
      ServiceUserDetail user
  ) {
    var applicationVersion = createNewApplication(type, operatorOuJson, user);
    applicationAssetService.createPrimaryAsset(applicationVersion, terminalWithOperatorJson);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_CREATED, INDUSTRY);
    return applicationVersion;
  }

  private ApplicationVersion createNewApplication(ApplicationType applicationType,
                                                  OrganisationUnitJson operatorOuJson,
                                                  ServiceUserDetail user) {
    var application = newApplication(applicationType, user, 0);
    applicationRepository.save(application);

    var applicationVersion =
        newApplicationVersion(application, 1, user, operatorOuJson.organisationUnitId(), operatorOuJson.name());
    applicationVersionRepository.save(applicationVersion);

    return applicationVersion;
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
      application.setApplicationNo(getNextApplicationNumber());
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

    var newApplicationVersion = newApplicationVersion(
        applicationVersion.getApplication(),
        applicationVersion.getVersion() + 1,
        user,
        applicationVersion.getPrimaryOperatorOuId(),
        applicationVersion.getCachedPrimaryOperatorName()
    );

    newApplicationVersion.setCaseOfficerWuaId(applicationVersion.getCaseOfficerWuaId());
    newApplicationVersion.setCamWuaId(applicationVersion.getCamWuaId());
    newApplicationVersion.setCurrentCaseOwner(applicationVersion.getCurrentCaseOwner());

    applicationVersionRepository.save(newApplicationVersion);

    return newApplicationVersion;
  }

  @Transactional
  public ApplicationVersion startApplicationRevision(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var application = applicationVersion.getApplication();

    // If a revision was previously started and then withdrawn, the user will be able to start a revision from the
    // previously consented application again, however we do not want to reuse the variation no from the withdrawn application.
    var newVariationNo = applicationRepository.getTipNonDeletedVariationNo(application) + 1;
    var newApplication = newApplication(application.getType(), user, newVariationNo);
    newApplication.setApplicationNo(application.getApplicationNo());
    applicationRepository.save(newApplication);

    var newApplicationVersion = newApplicationVersion(
        newApplication,
        1,
        user,
        applicationVersion.getPrimaryOperatorOuId(),
        applicationVersion.getCachedPrimaryOperatorName()
    );
    applicationVersionRepository.save(newApplicationVersion);
    return newApplicationVersion;
  }

  @Transactional
  public void consentApplication(ApplicationVersion applicationVersion) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.SUBMITTED.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application %d cannot be consented as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    applicationVersion.setStatus(ApplicationVersionStatus.CONSENTED);

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

  public int getNextApplicationNumber() {
    return applicationRepository.findLatestNonMigratedApplicationNumber()
        .map(latestApplicationNumber -> latestApplicationNumber + 1)
        .orElse(Integer.valueOf(applicationConfigurationProperties.applicationNoStartValue()));
  }

  public boolean isMigratedApplication(Application application) {
    return applicationVersionRepository.existsByApplicationAndMigratedTrue(application);
  }

  public boolean nonWithdrawnOrDeletedRevisionApplicationExists(Application application) {
    return applicationRepository.nonWithdrawnOrDeletedRevisionApplicationExists(application);
  }

  private Application newApplication(
      ApplicationType applicationType,
      ServiceUserDetail user,
      int variationNo
  ) {
    Application application = new Application();
    application.setType(applicationType);
    application.setCreatedDate(clock.instant());
    application.setCreatedByWuaId(user.wuaId());
    application.setVariationNo(variationNo);
    return application;
  }

  private ApplicationVersion newApplicationVersion(
      Application application,
      int version,
      ServiceUserDetail user,
      int primaryOperatorOuId,
      String cachedPrimaryOperatorName
  ) {
    ApplicationVersion applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(application);
    applicationVersion.setVersion(version);
    applicationVersion.setCreatedDateTime(clock.instant());
    applicationVersion.setCreatedByWuaId(user.wuaId());
    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);
    applicationVersion.setPrimaryOperatorOuId(primaryOperatorOuId);
    applicationVersion.setCachedPrimaryOperatorName(cachedPrimaryOperatorName);
    applicationVersion.setMigrated(false);
    return applicationVersion;
  }

  public Application getApplicationById(int applicationId) {
    return applicationRepository.findById(applicationId)
        .orElseThrow(() ->
            new EntityNotFoundException("Application with id %s not found".formatted(applicationId))
        );
  }
}

package uk.co.nstauthority.fieldconsents.application;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;

  private final ApplicationVersionRepository applicationVersionRepository;

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationAssetLicenceService applicationAssetLicenceService;

  private final ApplicationConfigurationProperties applicationConfigurationProperties;

  private final UserDetailService userDetailService;

  private final AceFlagService aceFlagService;

  public ApplicationService(ApplicationRepository applicationRepository,
                            ApplicationVersionRepository applicationVersionRepository,
                            ApplicationAssetService applicationAssetService,
                            ApplicationAssetLicenceService applicationAssetLicenceService,
                            ApplicationConfigurationProperties applicationConfigurationProperties,
                            UserDetailService userDetailService,
                            AceFlagService aceFlagService) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationAssetService = applicationAssetService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
    this.applicationConfigurationProperties = applicationConfigurationProperties;
    this.userDetailService = userDetailService;
    this.aceFlagService = aceFlagService;
  }

  private ApplicationVersion createNewApplication(ApplicationType applicationType, OrganisationUnitJson operatorOuJson) {
    Application application = createNewApplicationMasterRecord(applicationType);
    return createNewApplicationVersionRecord(application, operatorOuJson);
  }

  @Transactional
  public ApplicationVersion createNewApplicationForField(ApplicationType type,
                                                         FieldWithOperatorAndLicencesJson field,
                                                         OrganisationUnitJson operatorOuJson) {
    ApplicationVersion applicationVersion = createNewApplication(type, operatorOuJson);
    var applicationAsset = applicationAssetService.createPrimaryAsset(applicationVersion, field);
    applicationAssetLicenceService.createAssetLicences(applicationAsset, field);
    return applicationVersion;
  }

  @Transactional
  public ApplicationVersion createNewApplicationForTerminal(ApplicationType type,
                                                            TerminalWithOperatorJson terminalWithOperatorJson,
                                                            OrganisationUnitJson operatorOuJson) {
    ApplicationVersion applicationVersion = createNewApplication(type, operatorOuJson);
    applicationAssetService.createPrimaryAsset(applicationVersion, terminalWithOperatorJson);
    return applicationVersion;
  }

  private ApplicationVersion createNewApplicationVersionRecord(Application application,
                                                               OrganisationUnitJson operatorOuJson) {
    ApplicationVersion applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(application);
    applicationVersion.setVersion(1);
    applicationVersion.setCreatedDateTime(Instant.now());
    applicationVersion.setCreatedByWuaId(userDetailService.getUserDetail().wuaId());
    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);
    applicationVersion.setPrimaryOperatorOuId(operatorOuJson.organisationUnitId());
    applicationVersion.setCachedPrimaryOperatorName(operatorOuJson.name());
    return applicationVersionRepository.save(applicationVersion);
  }

  @Transactional
  public void submitApplication(ApplicationVersion applicationVersion) {
    var application = applicationVersion.getApplication();
    application.setApplicationNo(getApplicationNumber());
    submitApplicationVersion(applicationVersion);
    applicationRepository.save(application);
  }

  protected void submitApplicationVersion(ApplicationVersion applicationVersion) {
    if (!applicationVersion.getStatus().equals(ApplicationVersionStatus.IN_PROGRESS)) {
      throw new IllegalStateException(String.format("Application with id %s cannot be submitted",
          applicationVersion.getApplication().getId()));
    }
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);
    applicationVersion.setSubmittedDateTime(Instant.now());
    applicationVersion.setSubmittedByWuaId(userDetailService.getUserDetail().wuaId());
    applicationVersionRepository.save(applicationVersion);
    aceFlagService.autoSetAceFlag(applicationVersion);
  }

  public String generateApplicationReference(ApplicationVersion applicationVersion) {
    var application = applicationVersion.getApplication();
    return "%s/%d/%d (Version %d)".formatted(
        application.getType().getReferenceMnemonic(),
        application.getApplicationNo(),
        application.getVariationNo(),
        applicationVersion.getVersion());
  }

  protected int getApplicationNumber() {
    return applicationRepository.findLatestApplicationNumber()
        .map(latestApplicationNumber -> latestApplicationNumber + 1)
        .orElse(Integer.valueOf(applicationConfigurationProperties.applicationNoStartValue()));
  }

  @NotNull
  private Application createNewApplicationMasterRecord(ApplicationType applicationType) {
    Application application = new Application();
    application.setType(applicationType);
    application.setCreatedDate(Instant.now());
    application.setVariationNo(0);
    application.setCreatedByWuaId(userDetailService.getUserDetail().wuaId());
    return applicationRepository.save(application);
  }

  public Application getApplicationById(int applicationId) {
    return applicationRepository.findById(applicationId)
        .orElseThrow(() ->
            new EntityNotFoundException("Application with id %s not found".formatted(applicationId))
        );
  }
}

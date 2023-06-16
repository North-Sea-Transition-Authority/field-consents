package uk.co.nstauthority.fieldconsents.application;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_CREATED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_SUBMITTED;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;

  private final ApplicationVersionRepository applicationVersionRepository;

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationAssetLicenceService applicationAssetLicenceService;

  private final ApplicationConfigurationProperties applicationConfigurationProperties;

  private final AceFlagService aceFlagService;

  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  public ApplicationService(ApplicationRepository applicationRepository,
                            ApplicationVersionRepository applicationVersionRepository,
                            ApplicationAssetService applicationAssetService,
                            ApplicationAssetLicenceService applicationAssetLicenceService,
                            ApplicationConfigurationProperties applicationConfigurationProperties,
                            AceFlagService aceFlagService,
                            ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationAssetService = applicationAssetService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
    this.applicationConfigurationProperties = applicationConfigurationProperties;
    this.aceFlagService = aceFlagService;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
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
    applicationVersion.setCreatedDateTime(Instant.now());
    applicationVersion.setCreatedByWuaId(user.wuaId());
    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);
    applicationVersion.setPrimaryOperatorOuId(operatorOuJson.organisationUnitId());
    applicationVersion.setCachedPrimaryOperatorName(operatorOuJson.name());
    return applicationVersionRepository.save(applicationVersion);
  }

  @Transactional
  public void submitApplication(ApplicationVersion applicationVersion,
                                ServiceUserDetail user) {
    var application = applicationVersion.getApplication();
    application.setApplicationNo(getApplicationNumber());
    submitApplicationVersion(applicationVersion, user);
    applicationRepository.save(application);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_SUBMITTED, INDUSTRY);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_SUBMITTED, REGULATOR);
  }

  protected void submitApplicationVersion(ApplicationVersion applicationVersion,
                                          ServiceUserDetail user) {
    if (!applicationVersion.getStatus().equals(ApplicationVersionStatus.IN_PROGRESS)) {
      throw new IllegalStateException(String.format("Application with id %s cannot be submitted",
          applicationVersion.getApplication().getId()));
    }
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);
    applicationVersion.setSubmittedDateTime(Instant.now());
    applicationVersion.setSubmittedByWuaId(user.wuaId());
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
  private Application createNewApplicationMasterRecord(ApplicationType applicationType,
                                                       ServiceUserDetail user) {
    Application application = new Application();
    application.setType(applicationType);
    application.setCreatedDate(Instant.now());
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

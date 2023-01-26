package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;
import javax.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;

  private final ApplicationVersionRepository applicationVersionRepository;

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationAssetLicenceService applicationAssetLicenceService;

  @Autowired
  public ApplicationService(ApplicationRepository applicationRepository,
                            ApplicationVersionRepository applicationVersionRepository,
                            ApplicationAssetService applicationAssetService,
                            ApplicationAssetLicenceService applicationAssetLicenceService) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationAssetService = applicationAssetService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
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
    applicationVersion.setPrimaryOperatorOuId(operatorOuJson.organisationUnitId());
    applicationVersion.setCachedPrimaryOperatorName(operatorOuJson.name());
    return applicationVersionRepository.save(applicationVersion);
  }

  @NotNull
  private Application createNewApplicationMasterRecord(ApplicationType applicationType) {
    Application application = new Application();
    application.setType(applicationType);
    application.setCreatedDate(Instant.now());
    application.setCreatedByWuaId(1);
    return applicationRepository.save(application);
  }

  public Application getApplicationById(int applicationId) {
    return applicationRepository.findById(applicationId)
        .orElseThrow(() ->
            new EntityNotFoundException("Application with id %s not found".formatted(applicationId))
        );
  }
}

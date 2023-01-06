package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;
import javax.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.assets.ApplicationAssetService;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;

  private final ApplicationVersionRepository applicationVersionRepository;

  private final ApplicationAssetService applicationAssetService;

  @Autowired
  public ApplicationService(ApplicationRepository applicationRepository,
                            ApplicationVersionRepository applicationVersionRepository,
                            ApplicationAssetService applicationAssetService) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationAssetService = applicationAssetService;
  }

  private ApplicationVersion createNewApplication(ApplicationType applicationType) {
    Application application = createNewApplicationMasterRecord(applicationType);
    return createNewApplicationVersionRecord(application);
  }

  @Transactional
  public ApplicationVersion createNewApplicationForField(ApplicationType type, Integer fieldId) {
    ApplicationVersion applicationVersion = createNewApplication(type);
    applicationAssetService.createAssetRecordForField(applicationVersion, fieldId);
    return applicationVersion;
  }

  @Transactional
  public ApplicationVersion createNewApplicationForTerminal(ApplicationType type, Integer terminalId) {
    ApplicationVersion applicationVersion = createNewApplication(type);
    applicationAssetService.createAssetRecordForTerminal(applicationVersion, terminalId);
    return applicationVersion;
  }

  private ApplicationVersion createNewApplicationVersionRecord(Application application) {
    ApplicationVersion applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(application);
    applicationVersion.setVersion(1);
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

package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;
import javax.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

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

  private ApplicationVersion createNewApplication(ApplicationType applicationType, OrganisationUnitJson operatorOuJson) {
    Application application = createNewApplicationMasterRecord(applicationType);
    return createNewApplicationVersionRecord(application, operatorOuJson);
  }

  @Transactional
  public ApplicationVersion createNewApplicationForField(ApplicationType type, FieldJson fieldJson,
                                                         OrganisationUnitJson operatorOuJson) {
    ApplicationVersion applicationVersion = createNewApplication(type, operatorOuJson);
    applicationAssetService.createAssetRecordForPrimaryField(applicationVersion, fieldJson);
    return applicationVersion;
  }

  @Transactional
  public ApplicationVersion createNewApplicationForTerminal(ApplicationType type, TerminalJson terminalJson,
                                                            OrganisationUnitJson operatorOuJson) {
    ApplicationVersion applicationVersion = createNewApplication(type, operatorOuJson);
    applicationAssetService.createAssetRecordForTerminal(applicationVersion, terminalJson);
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

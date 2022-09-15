package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final ApplicationVersionRepository applicationVersionRepository;

  @Autowired
  public ApplicationService(ApplicationRepository applicationRepository,
                            ApplicationVersionRepository applicationVersionRepository) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
  }

  public ApplicationVersion createNewApplication(ApplicationType applicationType) {
    Application application = createNewApplicationMasterRecord(applicationType);
    return createNewApplicationVersionRecord(application);
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
}

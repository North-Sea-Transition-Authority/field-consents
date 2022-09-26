package uk.co.nstauthority.fieldconsents.application;

import java.util.Comparator;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationVersionService {

  private final ApplicationVersionRepository applicationVersionRepository;

  @Autowired
  public ApplicationVersionService(ApplicationVersionRepository applicationVersionRepository) {
    this.applicationVersionRepository = applicationVersionRepository;
  }

  public ApplicationVersion getApplicationVersionOrError(Integer applicationVersionId) {
    return getApplicationVersionById(applicationVersionId)
        .orElseThrow(() ->
            new EntityNotFoundException("Application version with id %s not found".formatted(applicationVersionId))
        );
  }

  private Optional<ApplicationVersion> getApplicationVersionById(Integer applicationVersionId) {
    return applicationVersionRepository.findById(applicationVersionId);
  }

  public ApplicationVersion getLatestApplicationVersionOrError(Integer applicationId) {
    return applicationVersionRepository.findAllByApplicationId(applicationId)
        .stream()
        .max(Comparator.comparing(ApplicationVersion::getVersion))
        .orElseThrow(() ->
            new EntityNotFoundException("Application version not found for application with id %s".formatted(applicationId))
        );
  }
}

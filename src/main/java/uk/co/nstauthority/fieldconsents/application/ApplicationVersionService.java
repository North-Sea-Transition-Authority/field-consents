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

  public ApplicationVersion getApplicationVersionById(Integer applicationVersionId) {
    return applicationVersionRepository.findById(applicationVersionId)
        .orElseThrow(() ->
            new EntityNotFoundException("Application version with id %s not found".formatted(applicationVersionId))
        );
  }

  public ApplicationVersion getLatestApplicationVersionByApplicationId(Integer applicationId) {
    return findLatestApplicationVersion(applicationId)
        .orElseThrow(() ->
            new EntityNotFoundException("Application version not found for application with id %s".formatted(applicationId))
        );
  }

  public Optional<ApplicationVersion> findLatestApplicationVersion(Integer applicationId) {
    return applicationVersionRepository.findAllByApplicationIdOrderByVersion(applicationId)
        .stream()
        .max(Comparator.comparing(ApplicationVersion::getVersion));
  }
}

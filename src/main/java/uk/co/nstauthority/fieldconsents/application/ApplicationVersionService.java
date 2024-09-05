package uk.co.nstauthority.fieldconsents.application;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  public List<ApplicationVersion> getLatestApplicationVersions(Collection<Integer> applicationIds) {
    return applicationVersionRepository.findLatestByApplicationIds(applicationIds);
  }

  public Optional<ApplicationVersion> findLatestApplicationVersion(Integer applicationId) {
    return getAllNonDeletedApplicationVersionsByApplicationId(applicationId).stream()
        .max(Comparator.comparing(ApplicationVersion::getVersion));
  }

  public List<ApplicationVersion> getAllApplicationVersionsByApplicationId(Integer applicationId) {
    return applicationVersionRepository.findAllByApplicationIdOrderByVersion(applicationId);
  }

  public List<ApplicationVersion> getAllNonDeletedApplicationVersionsByApplicationId(Integer applicationId) {
    return getAllApplicationVersionsByApplicationId(applicationId).stream()
        .filter(version -> !ApplicationVersionStatus.DELETED.equals(version.getStatus()))
        .toList();
  }

  @Transactional
  public void deleteApplicationVersion(ApplicationVersion applicationVersion) {
    if (ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())) {
      applicationVersion.setStatus(ApplicationVersionStatus.DELETED);
      applicationVersionRepository.save(applicationVersion);
    } else {
      throw new IllegalStateException("Cannot delete draft application with version id %d as application status is not %s"
          .formatted(applicationVersion.getId(), ApplicationVersionStatus.IN_PROGRESS));
    }
  }

  @Transactional
  public void withdrawApplicationVersion(ApplicationVersion applicationVersion) {
    if (!ApplicationVersionStatus.SUBMITTED.equals(applicationVersion.getStatus())) {
      throw new IllegalStateException(String.format("Application with id %s and status %s cannot be withdrawn",
          applicationVersion.getApplication().getId(), applicationVersion.getStatus().getDisplayName()));
    }
    applicationVersion.setStatus(ApplicationVersionStatus.WITHDRAWN);
    applicationVersionRepository.save(applicationVersion);
  }

  @Transactional
  public void closeApplicationVersion(ApplicationVersion applicationVersion) {
    applicationVersion.setStatus(ApplicationVersionStatus.CLOSED);
    applicationVersionRepository.save(applicationVersion);
  }
}

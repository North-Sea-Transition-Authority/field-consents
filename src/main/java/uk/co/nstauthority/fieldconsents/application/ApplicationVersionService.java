package uk.co.nstauthority.fieldconsents.application;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

  public ApplicationVersion getApplicationVersionByApplicationIdAndVersionNumber(Integer applicationId, Integer versionNumber) {
    return applicationVersionRepository.findAllByApplicationIdAndVersion(applicationId, versionNumber).stream()
        .filter(applicationVersion -> !ApplicationVersionStatus.DELETED.equals(applicationVersion.getStatus()))
        .findFirst()
        .orElseThrow(() ->
            new EntityNotFoundException("Application version not found for application with id %s and version number %s"
                .formatted(applicationId, versionNumber))
        );
  }

  public ApplicationVersion getSelectedApplicationVersionOrCurrent(
      ApplicationVersion applicationVersion,
      @Nullable Integer versionNumber
  ) {
    // if no version number is supplied, or it's the same as the supplied application version's number
    // then return the supplied application version
    if (versionNumber == null || applicationVersion.getVersion().equals(versionNumber)) {
      return applicationVersion;
    } else {
      // if the supplied version number is different to the supplied application version's number then get the
      // application version for the supplied version number
      return getApplicationVersionByApplicationIdAndVersionNumber(applicationVersion.getApplication().getId(), versionNumber);
    }
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

  public void deleteApplicationVersion(ApplicationVersion applicationVersion) {
    if (ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())) {
      applicationVersion.setStatus(ApplicationVersionStatus.DELETED);
      applicationVersionRepository.save(applicationVersion);
    } else {
      throw new IllegalStateException("Cannot delete draft application with version id %d as application status is not %s"
          .formatted(applicationVersion.getId(), ApplicationVersionStatus.IN_PROGRESS));
    }
  }

  public void withdrawApplicationVersion(ApplicationVersion applicationVersion) {
    if (!ApplicationVersionStatus.SUBMITTED.equals(applicationVersion.getStatus())) {
      throw new IllegalStateException(String.format("Application with id %s and status %s cannot be withdrawn",
          applicationVersion.getApplication().getId(), applicationVersion.getStatus().getDisplayName()));
    }
    applicationVersion.setStatus(ApplicationVersionStatus.WITHDRAWN);
    applicationVersionRepository.save(applicationVersion);
  }
}

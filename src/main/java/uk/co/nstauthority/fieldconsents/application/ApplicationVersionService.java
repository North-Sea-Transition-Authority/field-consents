package uk.co.nstauthority.fieldconsents.application;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;

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
    return getAllApplicationVersionsByApplicationId(applicationId)
        .stream()
        .filter(applicationVersion -> !ApplicationVersionStatus.DELETED.equals(applicationVersion.getStatus()))
        .max(Comparator.comparing(ApplicationVersion::getVersion));
  }

  public List<ApplicationVersion> getAllApplicationVersionsByApplicationId(Integer applicationId) {
    return applicationVersionRepository.findAllByApplicationIdOrderByVersion(applicationId);
  }

  public Optional<WebUserAccountId> findCaseOfficerWuaId(ApplicationVersion applicationVersion) {
    return Optional.ofNullable(applicationVersion.getCaseOfficerWuaId())
        .map(WebUserAccountId::from);
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

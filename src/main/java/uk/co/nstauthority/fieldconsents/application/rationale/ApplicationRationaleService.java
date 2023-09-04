package uk.co.nstauthority.fieldconsents.application.rationale;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;

@Service
public class ApplicationRationaleService {

  private final ApplicationRationaleRepository repository;
  private final ApplicationAssetService applicationAssetService;

  ApplicationRationaleService(
      ApplicationRationaleRepository repository,
      ApplicationAssetService applicationAssetService
  ) {
    this.repository = repository;
    this.applicationAssetService = applicationAssetService;
  }

  public boolean doesApplicationRationaleExistFor(ApplicationVersion applicationVersion) {
    return repository.existsApplicationRationaleByApplicationVersion(applicationVersion);
  }

  public Optional<ApplicationRationale> findByApplicationVersion(ApplicationVersion applicationVersion) {
    return repository.findByApplicationVersion(applicationVersion);
  }

  public List<AssetJson> getLocations(ApplicationVersion applicationVersion) {
    return applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.LOCATION);
  }

  public Optional<AssetJson> getHostLocation(ApplicationVersion applicationVersion) {
    return applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST).stream().findFirst();
  }
}

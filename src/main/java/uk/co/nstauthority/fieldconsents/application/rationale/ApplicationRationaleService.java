package uk.co.nstauthority.fieldconsents.application.rationale;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

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

  public String getAssetSearchUrl(ApplicationAsset primaryApplicationAsset) {
    var assetRole = primaryApplicationAsset.getAssetRole();
    if (assetRole != AssetRole.PRIMARY) {
      throw new IllegalArgumentException("Application asset must be primary. Was %s".formatted(assetRole));
    }

    var assetType = primaryApplicationAsset.getAssetType();
    return switch (assetType) {
      case FIELD -> switch (applicationAssetService.getShore(primaryApplicationAsset)) {
        case ONSHORE, UNKNOWN -> ReverseRouter.route(on(AssetRestController.class).searchFieldsAndTerminals(null));
        case OFFSHORE -> ReverseRouter.route(on(AssetRestController.class).searchFacilityAndHubAssets(null));
      };
      case TERMINAL -> ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null));
      default -> throw new IllegalArgumentException("Application asset should not be of type %s".formatted(assetType));
    };
  }

}

package uk.co.nstauthority.fieldconsents.application.assets;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ApplicationTerminalService {

  private final ApplicationAssetService applicationAssetService;

  public ApplicationTerminalService(ApplicationAssetService applicationAssetService) {
    this.applicationAssetService = applicationAssetService;
  }

  public List<Integer> findDistinctPrimaryTerminalIds() {
    return applicationAssetService.findAllPrimaryTerminalAssets()
        .stream()
        .map(ApplicationAsset::getTerminalId)
        .distinct()
        .toList();
  }
}

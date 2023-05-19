package uk.co.nstauthority.fieldconsents.application.assets;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ApplicationFieldService {

  private final ApplicationAssetService applicationAssetService;

  ApplicationFieldService(ApplicationAssetService applicationAssetService) {
    this.applicationAssetService = applicationAssetService;
  }

  public List<Integer> findDistinctPrimaryFieldIds() {
    return applicationAssetService.findAllPrimaryFieldAssets()
        .stream()
        .map(ApplicationAsset::getFieldId)
        .distinct()
        .toList();
  }
}
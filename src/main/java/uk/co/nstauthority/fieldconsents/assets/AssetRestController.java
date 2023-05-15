package uk.co.nstauthority.fieldconsents.assets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;

@RestController
public class AssetRestController {

  private final AssetService assetService;

  private final SearchSelectorService searchSelectorService;

  @Autowired
  public AssetRestController(AssetService assetService,
                             SearchSelectorService searchSelectorService) {
    this.assetService = assetService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/data-sources/assets")
  RestSearchResult searchAssetsForUser(@RequestParam("term") String assetName,
                                       ServiceUserDetail user) {
    return searchSelectorService.search(
        assetName,
        searchAssetName -> assetService.searchAssetsForUser(searchAssetName, user)
    );
  }

  @GetMapping("/data-sources/fields")
  RestSearchResult searchFields(@RequestParam("term") String fieldName) {
    return searchSelectorService.search(fieldName, assetService::searchFields);
  }
}

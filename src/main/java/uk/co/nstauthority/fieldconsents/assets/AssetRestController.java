package uk.co.nstauthority.fieldconsents.assets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;

@RestController
@AccessibleByServiceUsers
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
  public RestSearchResult searchAssetsForUser(@RequestParam(value = "term", required = false) String assetName,
                                              ServiceUserDetail user) {
    return searchSelectorService.search(
        assetName,
        searchAssetName -> assetService.searchAssetsForUser(searchAssetName, user)
    );
  }

  @GetMapping("/data-sources/all-assets")
  public RestSearchResult searchAllAssets(@RequestParam(value = "term", required = false) String assetName) {
    return searchSelectorService.search(assetName, assetService::searchAssets);
  }

  @GetMapping("/data-sources/terminal-assets")
  public RestSearchResult searchTerminalAssets(@RequestParam(value = "term", required = false) String assetName) {
    return searchSelectorService.search(assetName, assetService::searchTerminals);
  }

  @GetMapping("/data-sources/fields")
  RestSearchResult searchFields(@RequestParam("term") String fieldName) {
    return searchSelectorService.search(fieldName, assetService::searchFields);
  }
}

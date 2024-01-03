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

  @GetMapping("/data-sources/user-assets")
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
  public RestSearchResult searchTerminalAssets(@RequestParam(value = "term", required = false) String terminalName) {
    return searchSelectorService.search(terminalName, assetService::searchTerminals);
  }

  @GetMapping("/data-sources/field-assets")
  public RestSearchResult searchFieldAssets(@RequestParam(value = "term", required = false) String fieldName) {
    return searchSelectorService.search(fieldName, assetService::searchFields);
  }

  @GetMapping("/data-sources/user-terminal-assets")
  public RestSearchResult searchTerminalAssetsForUser(@RequestParam(value = "term", required = false) String terminalName,
                                                      ServiceUserDetail user) {
    return searchSelectorService.search(
        terminalName,
        searchTerminalName ->
            assetService.searchTerminalsForUser(searchTerminalName, user));
  }

  @GetMapping("/data-sources/user-field-assets")
  public RestSearchResult searchFieldAssetsForUser(@RequestParam(value = "term", required = false) String fieldName,
                                                   ServiceUserDetail user) {
    return searchSelectorService.search(
        fieldName,
        searchFieldName ->
            assetService.searchFieldsForUser(searchFieldName, user));
  }
}

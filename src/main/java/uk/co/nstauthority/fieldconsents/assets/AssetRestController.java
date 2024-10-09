package uk.co.nstauthority.fieldconsents.assets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchResult;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectorService;

@RestController
@AccessibleByServiceUsers
@RequestMapping("/data-sources/assets")
public class AssetRestController {

  private final AssetSearchService assetSearchService;
  private final SearchSelectorService searchSelectorService;

  AssetRestController(AssetSearchService assetSearchService, SearchSelectorService searchSelectorService) {
    this.assetSearchService = assetSearchService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/user")
  public RestSearchResult searchAssetsForUser(
      @RequestParam(value = "term", required = false) String assetName,
      ServiceUserDetail user
  ) {
    return searchSelectorService.search(
        assetName,
        searchAssetName -> assetSearchService.searchAssetsForUser(searchAssetName, user)
    );
  }

  @GetMapping("/fields-and-terminals")
  public RestSearchResult searchFieldsAndTerminals(@RequestParam(value = "term", required = false) String assetName) {
    return searchSelectorService.search(assetName, assetSearchService::searchFieldsAndTerminals);
  }

  @GetMapping("/terminals")
  public RestSearchResult searchTerminalAssets(@RequestParam(value = "term", required = false) String terminalName) {
    return searchSelectorService.search(terminalName, assetSearchService::searchTerminals);
  }

  @GetMapping("/fields")
  public RestSearchResult searchFieldAssets(@RequestParam(value = "term", required = false) String fieldName) {
    return searchSelectorService.search(fieldName, assetSearchService::searchFields);
  }

  @GetMapping("/facilities-and-hubs")
  public RestSearchResult searchFacilityAndHubAssets(@RequestParam(value = "term", required = false) String assetName) {
    return searchSelectorService.search(assetName, assetSearchService::searchFacilitiesAndHubs);
  }

  @GetMapping("/user-terminals")
  public RestSearchResult searchTerminalAssetsForUser(@RequestParam(value = "term", required = false) String terminalName,
                                                      ServiceUserDetail user) {
    return searchSelectorService.search(
        terminalName,
        searchTerminalName -> assetSearchService.searchTerminalsForUser(searchTerminalName, user)
    );
  }

  @GetMapping("/user-fields")
  public RestSearchResult searchFieldAssetsForUser(@RequestParam(value = "term", required = false) String fieldName,
                                                   ServiceUserDetail user) {
    return searchSelectorService.search(
        fieldName,
        searchFieldName -> assetSearchService.searchFieldsForUser(searchFieldName, user)
    );
  }
}

package uk.co.nstauthority.fieldconsents.assets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

  @GetMapping("/assets")
  RestSearchResult searchAssets(@RequestParam("term") String assetName) {
    return searchSelectorService.search(assetName, assetService.getAllAssets());
  }

}

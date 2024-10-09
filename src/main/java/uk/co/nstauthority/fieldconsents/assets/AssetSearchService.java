package uk.co.nstauthority.fieldconsents.assets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.facility.FacilityService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldSearchService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.hubs.HubService;
import uk.co.nstauthority.fieldconsents.assets.hubs.HubWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalSearchService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
class AssetSearchService {

  private static final String SEARCH_FIELDS_PURPOSE = "Assets search selector (search fields)";
  private static final String SEARCH_TERMINALS_PURPOSE = "Assets search selector (search terminals)";
  private static final String SEARCH_FACILITIES_PURPOSE = "Assets search selector (search facilities)";
  private static final String SEARCH_HUBS_PURPOSE = "Assets search selector (search hubs)";

  private final FieldSearchService fieldSearchService;
  private final TerminalSearchService terminalSearchService;
  private final FacilityService facilityService;
  private final HubService hubService;

  AssetSearchService(
      FieldSearchService fieldSearchService,
      TerminalSearchService terminalSearchService,
      FacilityService facilityService,
      HubService hubService
  ) {
    this.fieldSearchService = fieldSearchService;
    this.terminalSearchService = terminalSearchService;
    this.facilityService = facilityService;
    this.hubService = hubService;
  }

  public List<AssetWithOperatorJson> searchAssetsForUser(String assetName, ServiceUserDetail user) {
    return consolidateAssets(
        fieldSearchService.searchFieldsWithOperatorForUser(assetName, SEARCH_FIELDS_PURPOSE, user),
        terminalSearchService.searchTerminalsWithOperatorForUser(assetName, SEARCH_TERMINALS_PURPOSE, user)
    );
  }

  public List<TerminalWithOperatorJson> searchTerminalsForUser(String terminalName, ServiceUserDetail user) {
    return terminalSearchService.searchTerminalsWithOperatorForUser(terminalName, SEARCH_TERMINALS_PURPOSE, user);
  }

  public List<FieldWithOperatorJson> searchFieldsForUser(String fieldName, ServiceUserDetail user) {
    return fieldSearchService.searchFieldsWithOperatorForUser(fieldName, SEARCH_FIELDS_PURPOSE, user);
  }

  public List<AssetJson> searchFieldsAndTerminals(String assetName) {
    return consolidateAssets(
        fieldSearchService.searchFields(assetName, SEARCH_FIELDS_PURPOSE),
        terminalSearchService.searchTerminals(assetName, SEARCH_TERMINALS_PURPOSE)
    );
  }

  public List<AssetJson> searchTerminals(String assetName) {
    return consolidateAssets(Collections.emptyList(),
        terminalSearchService.searchTerminals(assetName, SEARCH_TERMINALS_PURPOSE));
  }

  public List<AssetJson> searchFields(String fieldName) {
    return consolidateAssets(fieldSearchService.searchFields(fieldName, SEARCH_FIELDS_PURPOSE), Collections.emptyList());
  }

  public List<? extends AssetJson> searchFacilitiesAndHubs(String searchTerm) {
    var suffix = HubWithOperatorJson.SUFFIX.toLowerCase();
    var assetName = searchTerm.toLowerCase().replace((suffix), "").trim();

    if (searchTerm.toLowerCase().contains((suffix))) {
      return hubService.searchHubs(assetName, SEARCH_HUBS_PURPOSE);
    }

    return consolidateAssets(
        facilityService.searchFacilities(assetName, SEARCH_FACILITIES_PURPOSE),
        hubService.searchHubs(assetName, SEARCH_HUBS_PURPOSE)
    );
  }

  private <A extends AssetJson> List<A> consolidateAssets(List<? extends A> assetsA, List<? extends A> assetsB) {
    return Stream
        .concat(assetsA.stream(), assetsB.stream())
        .sorted(Comparator.comparing(a -> a.getName().toLowerCase()))
        .toList();
  }
}

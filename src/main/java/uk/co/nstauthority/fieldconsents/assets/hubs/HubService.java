package uk.co.nstauthority.fieldconsents.assets.hubs;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.hub.HubApi;
import uk.co.fivium.energyportalapi.generated.client.HubProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.HubsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Hub;

@Service
public class HubService {

  private final HubApi hubApi;

  HubService(HubApi hubApi) {
    this.hubApi = hubApi;
  }

  public List<HubWithOperatorJson> searchHubs(String name, String requestPurpose) {
    var query = new HubsProjectionRoot().id().name().operator().organisationUnitId().name().root();
    return hubApi.search(name, query, new RequestPurpose(requestPurpose))
        .stream()
        .filter(this::hubHasOperator)
        .map(HubWithOperatorJson::from)
        .toList();
  }

  public Optional<HubWithOperatorJson> findHubWithOperator(Integer hubId, String requestPurpose) {
    var query = new HubProjectionRoot().id().name().operator().organisationUnitId().name().root();
    return hubApi.find(hubId, query, new RequestPurpose(requestPurpose))
        .filter(this::hubHasOperator)
        .map(HubWithOperatorJson::from);
  }

  public HubWithOperatorJson getHubWithOperator(Integer hubId, String requestPurpose) {
    return findHubWithOperator(hubId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Hub [%d] not found".formatted(hubId)));
  }

  private boolean hubHasOperator(Hub hub) {
    var operator = hub.getOperator();
    if (operator == null) {
      return false;
    }

    if (operator.getOrganisationUnitId() == null) {
      return false;
    }

    return operator.getName() != null;
  }

}

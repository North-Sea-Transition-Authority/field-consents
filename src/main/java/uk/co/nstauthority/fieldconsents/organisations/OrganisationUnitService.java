package uk.co.nstauthority.fieldconsents.organisations;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitsProjectionRoot;

@Service
public class OrganisationUnitService {

  private final OrganisationApi organisationApi;

  OrganisationUnitService(OrganisationApi organisationApi) {
    this.organisationApi = organisationApi;
  }

  public Optional<OrganisationUnitJson> findOrganisationUnitById(Integer organisationUnitId, String purpose) {
    var requestPurpose = new RequestPurpose(purpose);
    var requestedFields = new OrganisationUnitProjectionRoot()
        .organisationUnitId()
        .name();

    return organisationApi.findOrganisationUnit(organisationUnitId, requestedFields, requestPurpose)
        .map(OrganisationUnitJson::from);
  }

  public OrganisationUnitJson getOrganisationUnitById(Integer organisationUnitId, String purpose) {
    return findOrganisationUnitById(organisationUnitId, purpose)
        .orElseThrow(() -> new EntityNotFoundException("Organisation unit not found for id %s".formatted(organisationUnitId)));
  }

  public OrganisationUnitJson getOrganisationUnitByIdOrFallback(Integer organisationUnitId, String purpose,
                                                                String cachedOrganisationUnitName) {
    return findOrganisationUnitById(organisationUnitId, purpose)
        .orElseGet(() -> OrganisationUnitJson.fromCachedInformation(organisationUnitId, cachedOrganisationUnitName));
  }

  public Optional<OrganisationUnitWithGroupsJson> findOrganisationUnitWithGroupsById(Integer organisationUnitId, String purpose) {
    var requestPurpose = new RequestPurpose(purpose);
    var requestedFields = new OrganisationUnitProjectionRoot()
        .organisationUnitId()
        .name()
        .organisationGroups().organisationGroupId().name().root();

    return organisationApi.findOrganisationUnit(organisationUnitId, requestedFields, requestPurpose)
        .map(OrganisationUnitWithGroupsJson::from);
  }

  public OrganisationUnitWithGroupsJson getOrganisationUnitWithGroupsById(Integer organisationUnitId, String purpose) {
    return findOrganisationUnitWithGroupsById(organisationUnitId, purpose)
        .orElseThrow(() -> new EntityNotFoundException("Organisation unit not found for id %s".formatted(organisationUnitId)));
  }

  public List<OrganisationUnitJson> getOrganisationUnitsByIds(List<Integer> organisationUnitIds, String purpose) {
    if (organisationUnitIds.isEmpty()) {
      return Collections.emptyList();
    }

    var requestPurpose = new RequestPurpose(purpose);
    var organisationUnitsProjectionRoot = new OrganisationUnitsProjectionRoot().organisationUnitId().name();
    return organisationApi.getOrganisationUnitsByIds(organisationUnitIds, organisationUnitsProjectionRoot, requestPurpose)
        .stream()
        .map(OrganisationUnitJson::from)
        .toList();
  }
}

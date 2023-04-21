package uk.co.nstauthority.fieldconsents.energyportal.organisationgroup;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.energyportal.api.EnergyPortalApiWrapper;

@Service
public class OrganisationGroupQueryService {
  public static final OrganisationGroupsProjectionRoot ORGANISATION_GROUPS_PROJECTION_ROOT =
      new OrganisationGroupsProjectionRoot()
          .organisationGroupId()
          .name();
  public static final OrganisationGroupProjectionRoot ORGANISATION_GROUP_PROJECTION_ROOT = new OrganisationGroupProjectionRoot()
      .organisationGroupId()
      .name();
  public static final OrganisationGroupsProjectionRoot ORGANISATION_GROUPS_UNITS_PROJECTION_ROOT =
      new OrganisationGroupsProjectionRoot()
          .organisationGroupId()
          .name()
          .organisationUnits()
          .organisationUnitId()
          .name()
          .registeredNumber()
          .isDuplicate()
          .root();
  private final OrganisationApi organisationApi;
  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public OrganisationGroupQueryService(OrganisationApi organisationApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.organisationApi = organisationApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  public List<OrganisationGroupDto> getOrganisationGroupsByName(String name) {
    return energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
        organisationApi.searchOrganisationGroups(name, ORGANISATION_GROUPS_PROJECTION_ROOT, requestPurpose))
        .stream()
        .map(this::fromOrganisationGroup)
        .toList();
  }

  public List<OrganisationGroup> getOrganisationGroupsByIds(List<Integer> organisationGroups) {
    return energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) -> organisationApi
        .getAllOrganisationGroupsByIds(
            organisationGroups, ORGANISATION_GROUPS_UNITS_PROJECTION_ROOT, requestPurpose))
        .stream()
        .toList();
  }

  public Optional<OrganisationGroupDto> getOrganisationGroupById(Integer id) {
    return energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
        organisationApi.findOrganisationGroup(id, ORGANISATION_GROUP_PROJECTION_ROOT, requestPurpose))
        .map(this::fromOrganisationGroup);
  }

  private OrganisationGroupDto fromOrganisationGroup(OrganisationGroup organisationGroup) {
    var dto = new OrganisationGroupDto();
    dto.setOrganisationGroupId(organisationGroup.getOrganisationGroupId());
    dto.setOrganisationGroupName(organisationGroup.getName());
    return dto;
  }
}

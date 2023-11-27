package uk.co.nstauthority.fieldconsents.query;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ApplicationDataItemService {

  private final ApplicationDataItemDtoService applicationDataItemDtoService;

  ApplicationDataItemService(ApplicationDataItemDtoService applicationDataItemDtoService) {
    this.applicationDataItemDtoService = applicationDataItemDtoService;
  }

  public List<ApplicationDataItem> getItemsFromDtos(
      Collection<ApplicationDataItemDto> applicationDataItemDtos,
      Collection<OrganisationUnitJson> organisationUnitJsons,
      TeamType teamType,
      ServiceUserDetail user
  ) {
    if (applicationDataItemDtos.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitNamesById = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    var fieldJsonById = applicationDataItemDtoService
        .getFieldJsonMapFromApplicationDataItemDtos(applicationDataItemDtos);

    var portalUserDtoByWuaId = applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos);

    return applicationDataItemDtos.stream()
        .map(dataItemDto -> applicationDataItemDtoService.getApplicationDataItem(
            dataItemDto,
            user,
            teamType,
            organisationUnitNamesById,
            fieldJsonById,
            portalUserDtoByWuaId
        ))
        .toList();
  }

}

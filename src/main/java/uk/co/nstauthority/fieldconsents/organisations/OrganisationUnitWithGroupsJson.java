package uk.co.nstauthority.fieldconsents.organisations;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;

public record OrganisationUnitWithGroupsJson(
    Integer organisationUnitId,
    String name,
    List<OrganisationGroupDto> organisationGroups
) {

  public static OrganisationUnitWithGroupsJson from(OrganisationUnit organisationUnit) {
    return new OrganisationUnitWithGroupsJson(
        organisationUnit.getOrganisationUnitId(),
        organisationUnit.getName(),
        organisationUnit.getOrganisationGroups() == null || organisationUnit.getOrganisationGroups().isEmpty()
            ? Collections.emptyList()
            : organisationUnit.getOrganisationGroups()
            .stream()
            .map(OrganisationGroupDto::from)
            .toList()
    );
  }
}

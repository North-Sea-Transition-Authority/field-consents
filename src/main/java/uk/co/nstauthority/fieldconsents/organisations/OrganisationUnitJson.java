package uk.co.nstauthority.fieldconsents.organisations;

import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public record OrganisationUnitJson(
    Integer organisationUnitId,
    String name
) implements SearchSelectable {

  static OrganisationUnitJson from(OrganisationUnit organisationUnit) {
    return new OrganisationUnitJson(organisationUnit.getOrganisationUnitId(), organisationUnit.getName());
  }

  @Override
  public String getSelectionId() {
    return this.organisationUnitId.toString();
  }

  @Override
  public String getSelectionText() {
    return this.name;
  }
}



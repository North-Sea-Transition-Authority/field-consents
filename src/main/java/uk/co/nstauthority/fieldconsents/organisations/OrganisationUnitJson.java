package uk.co.nstauthority.fieldconsents.organisations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public record OrganisationUnitJson(
    Integer organisationUnitId,
    String name
) implements SearchSelectable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationUnitJson.class);

  static OrganisationUnitJson from(OrganisationUnit organisationUnit) {
    return new OrganisationUnitJson(organisationUnit.getOrganisationUnitId(), organisationUnit.getName());
  }

  public static OrganisationUnitJson fromCachedInformation(Integer organisationUnitId, String name) {
    LOGGER.warn("Had to fallback to organisation unit cache info for: id {}, name {}", organisationUnitId, name);
    return new OrganisationUnitJson(organisationUnitId, name);
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



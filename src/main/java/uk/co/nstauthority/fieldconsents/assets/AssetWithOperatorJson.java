package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

public interface AssetWithOperatorJson extends AssetJson {
  OrganisationUnitJson getOperatorJson();
}

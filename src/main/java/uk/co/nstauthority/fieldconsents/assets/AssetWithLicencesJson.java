package uk.co.nstauthority.fieldconsents.assets;

import java.util.List;
import uk.co.nstauthority.fieldconsents.licences.LicenceJson;

public interface AssetWithLicencesJson extends AssetJson {
  List<LicenceJson> getLicences();
}

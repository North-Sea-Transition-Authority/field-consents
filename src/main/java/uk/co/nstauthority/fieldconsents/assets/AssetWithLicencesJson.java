package uk.co.nstauthority.fieldconsents.assets;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.licences.LicenceJson;

public interface AssetWithLicencesJson extends AssetJson {
  List<LicenceJson> getLicences();

  default String getLicencesAsString() {
    return licencesExist()
        ? getLicences().stream().map(LicenceJson::licenceRef).collect(Collectors.joining(", "))
        : "None";
  }

  default boolean licencesExist() {
    return getLicences() != null && !getLicences().isEmpty();
  }

  default List<String> getLicenceReferences() {
    return licencesExist()
        ? getLicences().stream().map(LicenceJson::licenceRef).toList()
        : Collections.emptyList();
  }
}

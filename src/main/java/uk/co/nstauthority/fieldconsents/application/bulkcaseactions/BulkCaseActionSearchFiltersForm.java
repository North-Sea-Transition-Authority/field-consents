package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import java.util.Collections;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;

public record BulkCaseActionSearchFiltersForm(
    Integer operatorId,
    String fieldAssetKey,
    String terminalAssetKey,
    String caseOfficerWuaId,
    Set<GeographicArea> geographicAreas,
    Set<AceFlagStatus> aceFlagStatuses,
    Set<AssetTypeWithShore> assetTypesWithShore
) {

  public static BulkCaseActionSearchFiltersForm empty() {
    return new BulkCaseActionSearchFiltersForm(
        null,
        null,
        null,
        null,
        Collections.emptySet(),
        Collections.emptySet(),
        Collections.emptySet()
    );
  }

}

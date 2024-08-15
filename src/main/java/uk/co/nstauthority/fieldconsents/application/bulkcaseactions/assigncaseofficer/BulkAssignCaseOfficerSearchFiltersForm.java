package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;

public record BulkAssignCaseOfficerSearchFiltersForm(
    Integer operatorId,
    String fieldAssetKey,
    String terminalAssetKey,
    String caseOfficerWuaId,
    Set<GeographicArea> geographicAreas,
    Set<AceFlagStatus> aceFlagStatuses,
    Set<AssetTypeWithShore> assetTypesWithShore
) implements Serializable {

  @Serial
  private static final long serialVersionUID = 9121804835578033035L;

  static BulkAssignCaseOfficerSearchFiltersForm empty() {
    return new BulkAssignCaseOfficerSearchFiltersForm(
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

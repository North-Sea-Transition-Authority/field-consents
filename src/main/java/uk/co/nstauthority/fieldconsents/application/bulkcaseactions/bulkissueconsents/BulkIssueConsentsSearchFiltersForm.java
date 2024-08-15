package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;

public record BulkIssueConsentsSearchFiltersForm(
    Integer operatorId,
    String fieldAssetKey,
    String terminalAssetKey,
    Set<GeographicArea> geographicAreas,
    Set<AceFlagStatus> aceFlagStatuses,
    Set<AssetTypeWithShore> assetTypesWithShore
) implements Serializable {

  @Serial
  private static final long serialVersionUID = 8012347619178233021L;

  static BulkIssueConsentsSearchFiltersForm empty() {
    return new BulkIssueConsentsSearchFiltersForm(
        null,
        null,
        null,
        Collections.emptySet(),
        Collections.emptySet(),
        Collections.emptySet()
    );
  }
}

package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;

@Service
class BulkIssueConsentsSearchFilterService {

  static final String ORGANISATION_UNIT_LOOKUP_PURPOSE = "Lookup organisation unit for bulk action application data";

  private final ApplicationDataFilterFormService filterFormService;
  private final ApplicationDataFilterService applicationDataFilterService;

  BulkIssueConsentsSearchFilterService(
      ApplicationDataFilterFormService filterFormService,
      ApplicationDataFilterService applicationDataFilterService
  ) {
    this.filterFormService = filterFormService;
    this.applicationDataFilterService = applicationDataFilterService;
  }

  RestSearchItem getPrefilledOrganisation(Integer operatorId) {
    return filterFormService.getPrefilledOrganisation(operatorId, ORGANISATION_UNIT_LOOKUP_PURPOSE);
  }

  RestSearchItem getPrefilledAsset(String assetKey) {
    return filterFormService.getPrefilledAsset(assetKey);
  }

  List<Condition> getConditions(BulkIssueConsentsSearchFiltersForm filtersForm) {
    var conditions = new ArrayList<Condition>();

    Optional.ofNullable(filtersForm.operatorId())
        .map(applicationDataFilterService::getOperatorCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.geographicAreas())
        .filter(list -> !list.isEmpty())
        .map(applicationDataFilterService::getGeographicAreasQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.aceFlagStatuses())
        .filter(list -> !list.isEmpty())
        .map(applicationDataFilterService::getAceStatusCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.assetTypesWithShore())
        .filter(list -> !list.isEmpty())
        .map(applicationDataFilterService::getAssetTypesQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.fieldAssetKey())
        .flatMap(AssetKey::parse)
        .map(applicationDataFilterService::getFieldCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(filtersForm.terminalAssetKey())
        .flatMap(AssetKey::parse)
        .map(applicationDataFilterService::getTerminalCondition)
        .ifPresent(conditions::add);

    conditions.add(applicationDataFilterService.getReadyToGrantAndIssueApplicationsCondition());

    return conditions;
  }

}

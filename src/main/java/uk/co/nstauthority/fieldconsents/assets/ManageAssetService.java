package uk.co.nstauthority.fieldconsents.assets;

import java.util.List;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
public class ManageAssetService {

  private final ApplicationDataItemViewService applicationDataItemService;
  private final ApplicationDataFilterService applicationDataFilterService;
  private final TeamService teamService;

  ManageAssetService(
      ApplicationDataItemViewService applicationDataItemService,
      ApplicationDataFilterService applicationDataFilterService,
      TeamService teamService
  ) {
    this.applicationDataItemService = applicationDataItemService;
    this.applicationDataFilterService = applicationDataFilterService;
    this.teamService = teamService;
  }

  public List<ApplicationDataItemView> getApplicationDataItemViews(AssetKey assetKey, ServiceUserDetail user) {
    if (teamService.isRegulatorUser(user)) {
      return applicationDataItemService.getRegulatorApplicationDataItems(getConditions(assetKey), user);
    }

    if (teamService.isIndustryUser(user)) {
      return applicationDataItemService.getIndustryApplicationDataItems(getConditions(assetKey), user);
    }

    return List.of();
  }

  List<Condition> getConditions(AssetKey assetKey) {
    var condition = switch (assetKey.assetType()) {
      case FIELD -> applicationDataFilterService.getFieldCondition(assetKey);
      case TERMINAL -> applicationDataFilterService.getTerminalCondition(assetKey);
    };

    return List.of(condition);
  }
}

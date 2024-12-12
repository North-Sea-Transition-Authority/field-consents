package uk.co.nstauthority.fieldconsents.assets;

import java.util.List;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ManageAssetService {

  private final ApplicationDataItemViewService applicationDataItemService;
  private final ApplicationDataFilterService applicationDataFilterService;
  private final TeamQueryService teamQueryService;

  ManageAssetService(
      ApplicationDataItemViewService applicationDataItemService,
      ApplicationDataFilterService applicationDataFilterService,
      TeamQueryService teamQueryService
  ) {
    this.applicationDataItemService = applicationDataItemService;
    this.applicationDataFilterService = applicationDataFilterService;
    this.teamQueryService = teamQueryService;
  }

  public List<ApplicationDataItemView> getApplicationDataItemViews(AssetKey assetKey, ServiceUserDetail user) {
    if (teamQueryService.userIsMemberOfTeamType(user, TeamType.REGULATOR)) {
      return applicationDataItemService.getRegulatorApplicationDataItems(getConditions(assetKey), user);
    }

    if (teamQueryService.userIsMemberOfTeamType(user, TeamType.INDUSTRY)) {
      return applicationDataItemService.getIndustryApplicationDataItems(getConditions(assetKey), user);
    }

    return List.of();
  }

  List<Condition> getConditions(AssetKey assetKey) {
    var assetType = assetKey.assetType();
    var condition = switch (assetType) {
      case FIELD -> applicationDataFilterService.getFieldCondition(assetKey);
      case TERMINAL -> applicationDataFilterService.getTerminalCondition(assetKey);
      default -> throw new UnsupportedOperationException("Invalid asset type: %s".formatted(assetType.getDisplayName()));
    };

    return List.of(condition);
  }
}

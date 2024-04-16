package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jooq.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@ExtendWith(MockitoExtension.class)
class ManageAssetServiceTest {

  @Mock
  private ApplicationDataItemService applicationDataItemService;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @Mock
  private TeamService teamService;

  @InjectMocks
  @Spy
  private ManageAssetService manageAssetService;

  @Test
  void getApplicationDataItems_userIsRegulatorUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var assetKey = new AssetKey(1, AssetType.FIELD);

    var conditions = List.of(mock(Condition.class), mock(Condition.class));

    var applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());

    when(teamService.isRegulatorUser(user)).thenReturn(true);
    doReturn(conditions).when(manageAssetService).getConditions(assetKey);
    when(applicationDataItemService.getRegulatorApplicationDataItems(conditions, user)).thenReturn(applicationDataItems);

    assertThat(manageAssetService.getApplicationDataItems(assetKey, user)).isEqualTo(applicationDataItems);
  }

  @Test
  void getApplicationDataItems_userIsIndustryUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var assetKey = new AssetKey(1, AssetType.FIELD);

    var conditions = List.of(mock(Condition.class), mock(Condition.class));

    var applicationDataItems = List.of(ApplicationDataItemUtil.getApplicationDataItem());

    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(true);
    doReturn(conditions).when(manageAssetService).getConditions(assetKey);
    when(applicationDataItemService.getIndustryApplicationDataItems(conditions, user)).thenReturn(applicationDataItems);

    assertThat(manageAssetService.getApplicationDataItems(assetKey, user)).isEqualTo(applicationDataItems);
  }

  @Test
  void getApplicationDataItems_userIsNotRegulatorOrIndustryUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var assetKey = new AssetKey(1, AssetType.FIELD);

    when(teamService.isRegulatorUser(user)).thenReturn(false);
    when(teamService.isIndustryUser(user)).thenReturn(false);

    assertThat(manageAssetService.getApplicationDataItems(assetKey, user)).isEmpty();
  }

  @Test
  void getConditions_assetTypeIsField() {
    var assetKey = new AssetKey(1, AssetType.FIELD);

    var fieldCondition = mock(Condition.class);

    when(applicationDataFilterService.getFieldCondition(assetKey)).thenReturn(fieldCondition);

    assertThat(manageAssetService.getConditions(assetKey)).containsExactly(fieldCondition);
  }

  @Test
  void getConditions_assetTypeIsTerminal() {
    var assetKey = new AssetKey(1, AssetType.TERMINAL);

    var terminalCondition = mock(Condition.class);

    when(applicationDataFilterService.getTerminalCondition(assetKey)).thenReturn(terminalCondition);

    assertThat(manageAssetService.getConditions(assetKey)).containsExactly(terminalCondition);
  }
}
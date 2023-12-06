package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.jooq.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@ExtendWith(MockitoExtension.class)
class BulkCaseActionSearchFilterServiceTest {

  @Mock
  private ApplicationDataFilterFormService filterFormService;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @Mock
  private TeamService teamService;

  @InjectMocks
  private BulkCaseActionSearchFilterService bulkCaseActionSearchFilterService;

  private ServiceUserDetail caseManagerUser;

  @BeforeEach
  void setUp() {
    caseManagerUser = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getPrefilledOrganisation() {
    var restSearchItem = mock(RestSearchItem.class);
    when(filterFormService.getPrefilledOrganisation(eq(1), anyString())).thenReturn(restSearchItem);
    assertThat(bulkCaseActionSearchFilterService.getPrefilledOrganisation(1)).isEqualTo(restSearchItem);
  }

  @Test
  void getPrefilledAsset() {
    var restSearchItem = mock(RestSearchItem.class);
    when(filterFormService.getPrefilledAsset("1")).thenReturn(restSearchItem);
    assertThat(bulkCaseActionSearchFilterService.getPrefilledAsset("1")).isEqualTo(restSearchItem);
  }

  @Test
  void getConditions_emptyForm_isNotRegulator() {
    when(applicationDataFilterService.getCaseOfficerCondition(null, false)).thenReturn(Optional.empty());
    when(teamService.isRegulatorUser(caseManagerUser)).thenReturn(false);

    assertThat(bulkCaseActionSearchFilterService.getConditions(BulkCaseActionSearchFiltersForm.empty(), caseManagerUser)).isEmpty();
  }

  @Test
  void getConditions_emptyForm_isRegulator() {
    when(applicationDataFilterService.getCaseOfficerCondition(null, false)).thenReturn(Optional.empty());

    var condition = mock(Condition.class);
    when(teamService.isRegulatorUser(caseManagerUser)).thenReturn(true);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(condition);

    assertThat(bulkCaseActionSearchFilterService.getConditions(BulkCaseActionSearchFiltersForm.empty(), caseManagerUser))
        .containsExactly(condition);
  }

  @Test
  void getConditions_operatorId() {
    var form = new BulkCaseActionSearchFiltersForm(
        1,
        "1FIELD",
        "1TERMINAL",
        2L,
        true,
        Set.of(GeographicArea.CNS, GeographicArea.WOS),
        Set.of(AceFlagStatus.ACE),
        Set.of(AssetTypeWithShore.FIELD_ONSHORE)
    );

    var operatorCondition = mock(Condition.class);
    when(applicationDataFilterService.getOperatorCondition(form.operatorId())).thenReturn(operatorCondition);

    var geographicAreasCondition = mock(Condition.class);
    when(applicationDataFilterService.getGeographicAreasQueryCondition(form.geographicAreas())).thenReturn(geographicAreasCondition);

    var aceFlagStatusesCondition = mock(Condition.class);
    when(applicationDataFilterService.getAceStatusCondition(form.aceFlagStatuses())).thenReturn(aceFlagStatusesCondition);

    var assetTypesQueryCondition = mock(Condition.class);
    when(applicationDataFilterService.getAssetTypesQueryCondition(form.assetTypesWithShore())).thenReturn(assetTypesQueryCondition);

    var fieldAssetKeyCondition = mock(Condition.class);
    when(applicationDataFilterService.getFieldCondition(AssetKey.from(form.fieldAssetKey()))).thenReturn(fieldAssetKeyCondition);

    var terminalAssetKeyCondition = mock(Condition.class);
    when(applicationDataFilterService.getTerminalCondition(AssetKey.from(form.terminalAssetKey()))).thenReturn(terminalAssetKeyCondition);

    var caseOfficerCondition = mock(Condition.class);
    when(applicationDataFilterService.getCaseOfficerCondition(form.caseOfficerWuaId(), form.includeUnassignedCaseOfficer())).thenReturn(Optional.of(caseOfficerCondition));

    var userCondition = mock(Condition.class);
    when(teamService.isRegulatorUser(caseManagerUser)).thenReturn(true);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(userCondition);

    assertThat(bulkCaseActionSearchFilterService.getConditions(form, caseManagerUser)).containsExactly(
        operatorCondition,
        geographicAreasCondition,
        aceFlagStatusesCondition,
        assetTypesQueryCondition,
        fieldAssetKeyCondition,
        terminalAssetKeyCondition,
        caseOfficerCondition,
        userCondition
    );
  }

}

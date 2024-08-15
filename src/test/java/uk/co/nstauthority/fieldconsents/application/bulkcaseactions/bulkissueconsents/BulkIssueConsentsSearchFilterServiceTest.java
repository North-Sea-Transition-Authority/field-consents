package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.jooq.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;

@ExtendWith(MockitoExtension.class)
class BulkIssueConsentsSearchFilterServiceTest {

  @Mock
  private ApplicationDataFilterFormService filterFormService;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @InjectMocks
  private BulkIssueConsentsSearchFilterService bulkIssueConsentsSearchFilterService;

  @ParameterizedTest
  @ValueSource(ints = {1, 99})
  @NullSource
  void getPrefilledOrganisation(Integer operatorId) {
    var restSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    when(filterFormService.getPrefilledOrganisation(eq(operatorId), anyString())).thenReturn(restSearchItem);
    assertThat(bulkIssueConsentsSearchFilterService.getPrefilledOrganisation(operatorId)).isEqualTo(restSearchItem);
  }

  @ParameterizedTest
  @ValueSource(strings = {"1FIELD", "1TERMINAL"})
  @NullSource
  void getPrefilledAsset(String assetKey) {
    var restSearchItem = RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    when(filterFormService.getPrefilledAsset(assetKey)).thenReturn(restSearchItem);
    assertThat(bulkIssueConsentsSearchFilterService.getPrefilledAsset(assetKey)).isEqualTo(restSearchItem);
  }

  @Test
  void getConditions() {
    var operatorId = 123;
    var fieldAssetKey = "1FIELD";
    var terminalAssetKey = "1TERMINAL";
    var geographicAreas = EnumSet.allOf(GeographicArea.class);
    var aceStatusFlags = EnumSet.allOf(AceFlagStatus.class);
    var assetTypeWithShores = EnumSet.allOf(AssetTypeWithShore.class);

    var filtersForm = new BulkIssueConsentsSearchFiltersForm(
        operatorId,
        fieldAssetKey,
        terminalAssetKey,
        geographicAreas,
        aceStatusFlags,
        assetTypeWithShores
    );

    var operatorCondition = mock(Condition.class);
    when(applicationDataFilterService.getOperatorCondition(operatorId)).thenReturn(operatorCondition);

    var geographicAreasCondition = mock(Condition.class);
    when(applicationDataFilterService.getGeographicAreasQueryCondition(geographicAreas)).thenReturn(geographicAreasCondition);

    var aceStatusCondition = mock(Condition.class);
    when(applicationDataFilterService.getAceStatusCondition(aceStatusFlags)).thenReturn(aceStatusCondition);

    var assetTypesWithShoresCondition = mock(Condition.class);
    when(applicationDataFilterService.getAssetTypesQueryCondition(assetTypeWithShores)).thenReturn(assetTypesWithShoresCondition);

    var fieldCondition = mock(Condition.class);
    when(applicationDataFilterService.getFieldCondition(AssetKey.from(fieldAssetKey))).thenReturn(fieldCondition);

    var terminalCondition = mock(Condition.class);
    when(applicationDataFilterService.getTerminalCondition(AssetKey.from(terminalAssetKey))).thenReturn(terminalCondition);

    var readyToGrantAndIssueCondition = mock(Condition.class);
    when(applicationDataFilterService.getReadyToGrantAndIssueApplicationsCondition()).thenReturn(readyToGrantAndIssueCondition);

    assertThat(bulkIssueConsentsSearchFilterService.getConditions(filtersForm)).containsExactly(
        operatorCondition,
        geographicAreasCondition,
        aceStatusCondition,
        assetTypesWithShoresCondition,
        fieldCondition,
        terminalCondition,
        readyToGrantAndIssueCondition
    );
  }
}
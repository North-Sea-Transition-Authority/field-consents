package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilterFormService.ASSETS_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilterFormService.ORGANISATION_UNIT_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL_ASSET_KEY;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

@ExtendWith(MockitoExtension.class)
class WorkAreaFilterFormServiceTest {

  @Mock
  private AssetService assetService;

  @Mock
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  private WorkAreaFilterFormService workAreaFormService;

  @BeforeEach
  void setUp() {
    workAreaFormService = new WorkAreaFilterFormService(assetService, applicationDataFilterFormService);
  }

  @Test
  void from_withDefaultWorkAreaFilter() {
    var defaultFilter = ApplicationDataFilterFormTestUtil.getDefaultFilter();
    var workAreaForm = ApplicationDataFilterFormTestUtil.getWorkAreaFormForDefaultFilter();

    assertThat(workAreaFormService.getFromFilter(defaultFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }

  @Test
  void from_withConsentDurationsWorkAreaFilter() {
    var durationTypesFilter = ApplicationDataFilterFormTestUtil.getFilterWithDurationTypes();
    var workAreaForm = ApplicationDataFilterFormTestUtil.getWorkAreaFormForFilterWithDurationTypes();

    assertThat(workAreaFormService.getFromFilter(durationTypesFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }

  @Test
  void getPrefilledOrganisation_withEmptyOperator() {
    when(applicationDataFilterFormService.getPrefilledOrganisation(null, ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(EMPTY_REST_SEARCH_ITEM);

    assertThat(workAreaFormService.getPrefilledOrganisation(null)).isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisation_withOperator() {
    var searchRestItem = new RestSearchItem(orgUnit1Json.getSelectionId(), orgUnit1Json.getSelectionText());
    when(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(searchRestItem);

    assertThat(workAreaFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId()))
        .isEqualTo(searchRestItem);
  }

  @Test
  void getPrefilledOrganisation_withOperatorNotFound() {
    when(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(EMPTY_REST_SEARCH_ITEM);

    assertThat(workAreaFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId()))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledAsset_withEmptyAssetKey() {
    assertThat(workAreaFormService.getPrefilledAsset(null)).isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledAsset_withFieldAssetKey() {
    doReturn(Optional.of(field1AssetJson)).when(assetService).getAsset(AssetKey.from(FIELD_ASSET_KEY), ASSETS_LOOKUP_PURPOSE);

    assertThat(workAreaFormService.getPrefilledAsset(FIELD_ASSET_KEY))
        .isEqualTo(new RestSearchItem(field1AssetJson.getSelectionId(), field1AssetJson.getSelectionText()));
  }

  @Test
  void getPrefilledAsset_withTerminalAssetKey() {
    doReturn(Optional.of(terminal1AssetJson)).when(assetService).getAsset(AssetKey.from(TERMINAL_ASSET_KEY), ASSETS_LOOKUP_PURPOSE);

    assertThat(workAreaFormService.getPrefilledAsset(TERMINAL_ASSET_KEY))
        .isEqualTo(new RestSearchItem(terminal1AssetJson.getSelectionId(), terminal1AssetJson.getSelectionText()));
  }
}

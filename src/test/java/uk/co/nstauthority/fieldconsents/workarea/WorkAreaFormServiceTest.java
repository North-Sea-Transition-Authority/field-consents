package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormService.ORGANISATION_UNIT_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormServiceTestUtil.FIELD_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormServiceTestUtil.TERMINAL_ASSET_KEY;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class WorkAreaFormServiceTest {

  @Mock
  private AssetService assetService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  private WorkAreaFormService workAreaFormService;

  @BeforeEach
  void setUp() {
    workAreaFormService = new WorkAreaFormService(assetService, organisationUnitService);
  }

  @Test
  void from_withDefaultWorkAreaFilter() {
    var defaultFilter = WorkAreaFormServiceTestUtil.getDefaultFilter();
    var workAreaForm = WorkAreaFormServiceTestUtil.getWorkAreaFormForDefaultFilter();

    assertThat(workAreaFormService.getFromFilter(defaultFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }

  @Test
  void from_withConsentDurationsWorkAreaFilter() {
    var durationTypesFilter = WorkAreaFormServiceTestUtil.getFilterWithDurationTypes();
    var workAreaForm = WorkAreaFormServiceTestUtil.getWorkAreaFormForFilterWithDurationTypes();

    assertThat(workAreaFormService.getFromFilter(durationTypesFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }

  @Test
  void getPrefilledOrganisation_withEmptyOperator() {
    assertThat(workAreaFormService.getPrefilledOrganisation(null)).isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisation_withOperator() {
    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(orgUnit1Json));

    assertThat(workAreaFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId()))
        .isEqualTo(new RestSearchItem(orgUnit1Json.getSelectionId(), orgUnit1Json.getSelectionText()));
  }

  @Test
  void getPrefilledOrganisation_withOperatorNotFound() {
    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.empty());

    assertThat(workAreaFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId()))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledAsset_withEmptyAssetKey() {
    when(assetService.getAssetFromKey(null)).thenReturn(Optional.empty());
    assertThat(workAreaFormService.getPrefilledAsset(null)).isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledAsset_withFieldAssetKey() {
    when(assetService.getAssetFromKey(FIELD_ASSET_KEY)).thenReturn(Optional.of(field1AssetJson));
    assertThat(workAreaFormService.getPrefilledAsset(FIELD_ASSET_KEY))
        .isEqualTo(new RestSearchItem(field1AssetJson.getSelectionId(), field1AssetJson.getSelectionText()));
  }

  @Test
  void getPrefilledAsset_withTerminalAssetKey() {
    when(assetService.getAssetFromKey(TERMINAL_ASSET_KEY)).thenReturn(Optional.of(terminal1AssetJson));
    assertThat(workAreaFormService.getPrefilledAsset(TERMINAL_ASSET_KEY))
        .isEqualTo(new RestSearchItem(terminal1AssetJson.getSelectionId(), terminal1AssetJson.getSelectionText()));
  }
}
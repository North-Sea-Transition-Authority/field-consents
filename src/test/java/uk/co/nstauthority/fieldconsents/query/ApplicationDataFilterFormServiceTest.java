package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService.ORGANISATION_UNIT_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.TERMINAL1_ASSET_KEY;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class ApplicationDataFilterFormServiceTest {

  @Mock
  private AssetService assetService;
  @Mock
  private OrganisationUnitService organisationUnitService;

  @InjectMocks
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  @Test
  void getPrefilledOrganisation_withEmptyOperator() {
    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(null, ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisation_withOperator() {
    var searchRestItem = new RestSearchItem(orgUnit1Json.getSelectionId(), orgUnit1Json.getSelectionText());

    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(orgUnit1Json));

    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(searchRestItem);
  }

  @Test
  void getPrefilledOrganisation_withOperatorNotFound() {
    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.empty());

    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisation_withNullOperator() {
    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(null))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisation_withNonNullOperator() {
    var searchRestItem = new RestSearchItem(orgUnit1Json.getSelectionId(), orgUnit1Json.getSelectionText());

    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(orgUnit1Json));

    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(searchRestItem);
  }

  @Test
  void getPrefilledOrganisation_withNonNullOperatorNotFound() {
    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.empty());

    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledAsset_withEmptyAssetKey() {
    assertThat(applicationDataFilterFormService.getPrefilledAsset(null)).isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledAsset_withFieldAssetKey() {
    doReturn(Optional.of(field1AssetJson)).when(assetService).findAsset(AssetKey.from(FIELD1_ASSET_KEY));

    assertThat(applicationDataFilterFormService.getPrefilledAsset(FIELD1_ASSET_KEY))
        .isEqualTo(new RestSearchItem(field1AssetJson.getSelectionId(), field1AssetJson.getSelectionText()));
  }

  @Test
  void getPrefilledAsset_withTerminalAssetKey() {
    doReturn(Optional.of(terminal1AssetJson)).when(assetService).findAsset(AssetKey.from(TERMINAL1_ASSET_KEY));

    assertThat(applicationDataFilterFormService.getPrefilledAsset(TERMINAL1_ASSET_KEY))
        .isEqualTo(new RestSearchItem(terminal1AssetJson.getSelectionId(), terminal1AssetJson.getSelectionText()));
  }
}

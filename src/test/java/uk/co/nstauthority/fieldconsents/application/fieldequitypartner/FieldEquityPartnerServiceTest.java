package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@ExtendWith(MockitoExtension.class)
class FieldEquityPartnerServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldApi fieldApi;

  @Spy
  @InjectMocks
  private FieldEquityPartnerService fieldEquityPartnerService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getFieldEquityPartnersView() {
    var fieldEquityPartnerNames = List.of("a", "b", "c");

    doReturn(fieldEquityPartnerNames).when(fieldEquityPartnerService).getFieldEquityPartnerNames(applicationVersion);

    var fieldEquityPartnersView = fieldEquityPartnerService.getFieldEquityPartnersView(applicationVersion);

    assertThat(fieldEquityPartnersView).isEqualTo(new FieldEquityPartnersView(fieldEquityPartnerNames));
  }

  @Test
  void getFieldEquityPartnerNames() {
    var applicationAssets = List.of(
      ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).withAssetRole(AssetRole.PRIMARY).withAssetId(1).build(),
      ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).withAssetRole(AssetRole.SECONDARY).withAssetId(2).build(),
      ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).withAssetRole(AssetRole.SECONDARY).withAssetId(3).build()
    );

    var fields = List.of(
        getFieldWithFieldEquityPartnerName("b"),
        getFieldWithFieldEquityPartnerName("c"),
        getFieldWithFieldEquityPartnerName("a")
    );

    var assetIds = applicationAssets.stream().map(ApplicationAsset::getAssetId).toList();

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(applicationVersion, AssetType.FIELD, EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY))).thenReturn(applicationAssets);
    when(fieldApi.getFieldsByIds(eq(assetIds), any(FieldsProjectionRoot.class), any(RequestPurpose.class))).thenReturn(fields);

    assertThat(fieldEquityPartnerService.getFieldEquityPartnerNames(applicationVersion)).containsExactly("a", "b", "c");
  }

  private Field getFieldWithFieldEquityPartnerName(String fieldEquityPartnerName) {
    return Field.newBuilder()
        .fieldEquityPartners(List.of(
            FieldEquityPartner.newBuilder()
                .organisationUnit(OrganisationUnit.newBuilder().name(fieldEquityPartnerName).build())
                .build()
        ))
        .build();
  }

}

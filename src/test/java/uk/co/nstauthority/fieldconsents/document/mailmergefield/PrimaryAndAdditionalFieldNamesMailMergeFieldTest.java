package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class PrimaryAndAdditionalFieldNamesMailMergeFieldTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @InjectMocks
  private PrimaryAndAdditionalFieldNamesMailMergeField primaryAndAdditionalFieldNamesMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(primaryAndAdditionalFieldNamesMailMergeField.getMnemonic())
        .isEqualTo("PRIMARY_AND_ADDITIONAL_FIELD_NAMES");
  }

  @Test
  void getDescription() {
    assertThat(primaryAndAdditionalFieldNamesMailMergeField.getDescription())
        .isEqualTo("The names of the primary and additional fields on the application");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(primaryAndAdditionalFieldNamesMailMergeField.isApplicable(template))
        .isEqualTo(documentTemplateType == DocumentTemplateType.FIELD_FLARE_CONSENT
            || documentTemplateType == DocumentTemplateType.FIELD_VENT_CONSENT);
  }

  @Test
  void resolve_noAssets() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(
        applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
            applicationVersion,
            AssetType.FIELD,
            Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
        )
    ).thenReturn(List.of());

    assertThatThrownBy(() -> primaryAndAdditionalFieldNamesMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @Test
  void resolve_noPrimaryAsset() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAssets = List.of(
        ApplicationAssetTestUtil.newBuilder()
            .withAssetType(AssetType.FIELD)
            .withAssetRole(AssetRole.SECONDARY)
            .build()
    );

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(
        applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
            applicationVersion,
            AssetType.FIELD,
            Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
        )
    ).thenReturn(applicationAssets);

    assertThatThrownBy(() -> primaryAndAdditionalFieldNamesMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @ParameterizedTest
  @MethodSource("getResolveArguments")
  void resolve(List<ApplicationAsset> applicationAssets, List<FieldJson> fieldJsons, String expected) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var fieldIds = applicationAssets.stream().map(ApplicationAsset::getAssetId).toList();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(
        applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
            applicationVersion,
            AssetType.FIELD,
            Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
        )
    ).thenReturn(applicationAssets);
    when(fieldService.findFieldsByIds(fieldIds, "Fields lookup for PRIMARY_AND_ADDITIONAL_FIELD_NAMES mail merge field"))
        .thenReturn(fieldJsons);

    assertThat(primaryAndAdditionalFieldNamesMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(expected));
  }

  private static Stream<Arguments> getResolveArguments() {
    var primaryFieldAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .withAssetId(5)
        .withAssetRole(AssetRole.PRIMARY)
        .build();

    var secondaryFieldAsset1 = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .withAssetId(9)
        .withAssetRole(AssetRole.SECONDARY)
        .build();

    var secondaryFieldAsset2 = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .withAssetId(2)
        .withAssetRole(AssetRole.SECONDARY)
        .build();

    var secondaryFieldAsset3 = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .withAssetId(7)
        .withAssetRole(AssetRole.SECONDARY)
        .build();

    var secondaryFieldAsset4 = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .withAssetId(1)
        .withAssetRole(AssetRole.SECONDARY)
        .build();

    var secondaryFieldAsset5 = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .withAssetId(4)
        .withAssetRole(AssetRole.SECONDARY)
        .build();

    var secondaryFieldAsset6 = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .withAssetId(8)
        .withAssetRole(AssetRole.SECONDARY)
        .build();

    var primaryFieldJson = FieldJson.fromCachedInformation(primaryFieldAsset.getAssetId(), "Primary field name");

    var secondaryFieldJson1 =
        FieldJson.fromCachedInformation(secondaryFieldAsset1.getAssetId(), "Secondary field name 1");

    var secondaryFieldJson2 =
        FieldJson.fromCachedInformation(secondaryFieldAsset2.getAssetId(), "Secondary field name 2");

    var secondaryFieldJson3 =
        FieldJson.fromCachedInformation(secondaryFieldAsset3.getAssetId(), "Secondary field name 3");

    var secondaryFieldJson4 =
        FieldJson.fromCachedInformation(secondaryFieldAsset4.getAssetId(), "Secondary field name a");

    var secondaryFieldJson5 =
        FieldJson.fromCachedInformation(secondaryFieldAsset5.getAssetId(), "Secondary field name b");

    var secondaryFieldJson6 =
        FieldJson.fromCachedInformation(secondaryFieldAsset6.getAssetId(), "Secondary field name c");

    return Stream.of(
        Arguments.of(
            List.of(primaryFieldAsset),
            List.of(primaryFieldJson),
            "Primary field name"
        ),
        // The ordering of the below assets is intentional to verify the primary field name is included first and the
        // secondary field names are sorted in the resolved string.
        Arguments.of(
            List.of(secondaryFieldAsset1, primaryFieldAsset),
            List.of(secondaryFieldJson1, primaryFieldJson),
            "Primary field name and Secondary field name 1"
        ),
        Arguments.of(
            List.of(secondaryFieldAsset2, primaryFieldAsset, secondaryFieldAsset1),
            List.of(secondaryFieldJson2, primaryFieldJson, secondaryFieldJson1),
            "Primary field name, Secondary field name 1 and Secondary field name 2"
        ),
        Arguments.of(
            List.of(
                secondaryFieldAsset2,
                primaryFieldAsset,
                secondaryFieldAsset5,
                secondaryFieldAsset1,
                secondaryFieldAsset3,
                secondaryFieldAsset6,
                secondaryFieldAsset4
            ),
            List.of(
                secondaryFieldJson2,
                primaryFieldJson,
                secondaryFieldJson5,
                secondaryFieldJson1,
                secondaryFieldJson3,
                secondaryFieldJson6,
                secondaryFieldJson4
            ),
            """
            Primary field name, Secondary field name 1, Secondary field name 2, Secondary field name 3, Secondary \
            field name a, Secondary field name b and Secondary field name c\
            """
        )
    );
  }
}

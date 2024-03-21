package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class FacilityNameMailMergeFieldTest {

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private TerminalService terminalService;

  @InjectMocks
  private FacilityNameMailMergeField facilityNameMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(facilityNameMailMergeField.getMnemonic()).isEqualTo("FACILITY_NAME");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(facilityNameMailMergeField.isApplicable(documentTemplateDto))
        .isEqualTo(documentTemplateType.isApplicableToTerminalApplications());
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, names = "TERMINAL", mode = EnumSource.Mode.EXCLUDE)
  void resolve_primaryAssetTypeIsNotTerminal(AssetType assetType) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var applicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(assetType)
        .build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);

    assertThatThrownBy(() -> facilityNameMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @Test
  void primaryAssetTypeIsTerminal() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var applicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.TERMINAL)
        .build();

    var terminalName = "Sullom Voe";
    var terminalJson = new TerminalJson(null, terminalName, null);

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);
    when(terminalService.getTerminal(applicationAsset.getAssetId(), "Terminal lookup for FACILITY_NAME mail merge field"))
        .thenReturn(terminalJson);

    assertThat(facilityNameMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(terminalName));
  }
}

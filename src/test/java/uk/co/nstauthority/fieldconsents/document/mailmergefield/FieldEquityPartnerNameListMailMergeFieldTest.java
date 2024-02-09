package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class FieldEquityPartnerNameListMailMergeFieldTest {

  private static final String MNEMONIC = "FIELD_EQUITY_PARTNER_NAME_LIST";
  private static final String DESCRIPTION = "A list of field equity partner names associated to the fields on this application";

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private FieldEquityPartnerService fieldEquityPartnerService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @InjectMocks
  private FieldEquityPartnerNameListMailMergeField fieldEquityPartnerNameListMailMergeField;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void getMnemonic() {
    assertThat(fieldEquityPartnerNameListMailMergeField.getMnemonic()).isEqualTo(MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(fieldEquityPartnerNameListMailMergeField.getDescription()).isEqualTo(DESCRIPTION);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().withMnemonic(documentTemplateType.getMnemonic()).build();

    assertThat(fieldEquityPartnerNameListMailMergeField.isApplicable(documentTemplateDto))
        .isEqualTo(DocumentTemplateType.isField(documentTemplateType));
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var fieldEquityPartnerNames = List.of("first", "second", "third");

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(fieldEquityPartnerService.getFieldEquityPartnerNames(applicationVersion)).thenReturn(fieldEquityPartnerNames);

    assertThat(fieldEquityPartnerNameListMailMergeField.resolve(documentInstanceDto)).isEqualTo("first, second and third");
  }

}

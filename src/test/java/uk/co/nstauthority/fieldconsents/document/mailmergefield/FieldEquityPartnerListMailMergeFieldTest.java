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
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FormattedFieldEquityPartner;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class FieldEquityPartnerListMailMergeFieldTest {

  private static final String MNEMONIC = "FIELD_EQUITY_PARTNER_LIST";
  private static final String DESCRIPTION = "A list of field equity partners associated to the fields on this application. Includes the organisation name and registered number";

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @Mock
  private FieldEquityPartnerService fieldEquityPartnerService;

  @InjectMocks
  private FieldEquityPartnerListMailMergeField fieldEquityPartnerListMailMergeField;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void getMnemonic() {
    assertThat(fieldEquityPartnerListMailMergeField.getMnemonic()).isEqualTo(MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(fieldEquityPartnerListMailMergeField.getDescription()).isEqualTo(DESCRIPTION);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(fieldEquityPartnerListMailMergeField.isApplicable(documentTemplateDto))
        .isEqualTo(documentTemplateType.isApplicableToFieldApplications());
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var formattedFieldEquityPartner = new FormattedFieldEquityPartner("first", "001");
    var formattedFieldEquityPartners = List.of(formattedFieldEquityPartner);

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(fieldEquityPartnerService.getFormattedFieldEquityPartners(applicationVersion)).thenReturn(formattedFieldEquityPartners);

    assertThat(fieldEquityPartnerListMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(formattedFieldEquityPartner.getFormattedValue()));
  }
}

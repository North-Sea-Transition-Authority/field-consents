package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

class DigitalSignatureMailMergeFieldTest {

  private final DigitalSignatureMailMergeField digitalSignatureMailMergeField = new DigitalSignatureMailMergeField();

  @Test
  void getMnemonic() {
    assertThat(digitalSignatureMailMergeField.getMnemonic()).isEqualTo("DIGITAL_SIGNATURE");
  }

  @Test
  void getDescription() {
    assertThat(digitalSignatureMailMergeField.getDescription()).isNotBlank();
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    if (documentTemplateType == DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER) {
      assertThat(digitalSignatureMailMergeField.isApplicable(documentTemplateDto)).isFalse();
    } else {
      assertThat(digitalSignatureMailMergeField.isApplicable(documentTemplateDto)).isTrue();
    }
  }

  @Test
  void resolve() {
    var instanceDto = DocumentInstanceDtoTestUtil.builder().build();
    assertThat(digitalSignatureMailMergeField.resolve(instanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(DigitalSignatureMailMergeField.SIGNATURE_PLACEHOLDER_TEXT));
  }
}

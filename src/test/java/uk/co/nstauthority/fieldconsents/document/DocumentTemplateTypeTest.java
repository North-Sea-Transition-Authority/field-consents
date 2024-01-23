package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DocumentTemplateTypeTest {

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void getMnemonic(DocumentTemplateType documentTemplateType) {
    assertThat(documentTemplateType.getMnemonic()).isEqualTo(documentTemplateType.name());
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void getByMnemonic(DocumentTemplateType documentTemplateType) {
    var mnemonic = documentTemplateType.getMnemonic();

    assertThat(DocumentTemplateType.getByMnemonic(mnemonic)).isEqualTo(documentTemplateType);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isField(DocumentTemplateType documentTemplateType) {
    assertThat(DocumentTemplateType.isField(documentTemplateType)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isConsent(DocumentTemplateType documentTemplateType) {
    assertThat(DocumentTemplateType.isConsent(documentTemplateType)).isTrue();
  }
}

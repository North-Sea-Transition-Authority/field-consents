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
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "FIELD_PRODUCTION_CONSENT", "FIELD_FLARE_CONSENT", "FIELD_VENT_CONSENT" },
      mode = EnumSource.Mode.INCLUDE
  )
  void isField_documentTemplateTypeIsField(DocumentTemplateType documentTemplateType) {
    assertThat(DocumentTemplateType.isField(documentTemplateType)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "FIELD_PRODUCTION_CONSENT", "FIELD_FLARE_CONSENT", "FIELD_VENT_CONSENT" },
      mode = EnumSource.Mode.EXCLUDE
  )
  void isField_documentTemplateTypeIsNotField(DocumentTemplateType documentTemplateType) {
    assertThat(DocumentTemplateType.isField(documentTemplateType)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "TERMINAL_FLARE_CONSENT", "TERMINAL_VENT_CONSENT" },
      mode = EnumSource.Mode.INCLUDE
  )
  void isTerminal_documentTemplateTypeIsTerminal(DocumentTemplateType documentTemplateType) {
    assertThat(DocumentTemplateType.isTerminal(documentTemplateType)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "TERMINAL_FLARE_CONSENT", "TERMINAL_VENT_CONSENT" },
      mode = EnumSource.Mode.EXCLUDE
  )
  void isTerminal_documentTemplateTypeIsNotTerminal(DocumentTemplateType documentTemplateType) {
    assertThat(DocumentTemplateType.isTerminal(documentTemplateType)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isConsent(DocumentTemplateType documentTemplateType) {
    assertThat(DocumentTemplateType.isConsent(documentTemplateType)).isTrue();
  }
}

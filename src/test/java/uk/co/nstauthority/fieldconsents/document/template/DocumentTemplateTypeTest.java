package uk.co.nstauthority.fieldconsents.document.template;

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
      names = { "FIELD_PRODUCTION_CONSENT", "FIELD_FLARE_CONSENT", "FIELD_VENT_CONSENT", "FLARE_AND_COMMISSIONING_LETTER" },
      mode = EnumSource.Mode.INCLUDE
  )
  void isApplicableToFieldApplications_documentTemplateTypeIsApplicable(DocumentTemplateType documentTemplateType) {
    assertThat(documentTemplateType.isApplicableToFieldApplications()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "FIELD_PRODUCTION_CONSENT", "FIELD_FLARE_CONSENT", "FIELD_VENT_CONSENT", "FLARE_AND_COMMISSIONING_LETTER" },
      mode = EnumSource.Mode.EXCLUDE
  )
  void isApplicableToFieldApplications_documentTemplateTypeIsNotApplicable(DocumentTemplateType documentTemplateType) {
    assertThat(documentTemplateType.isApplicableToFieldApplications()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "TERMINAL_FLARE_CONSENT", "TERMINAL_VENT_CONSENT" },
      mode = EnumSource.Mode.INCLUDE
  )
  void isApplicableToTerminalApplications_documentTemplateTypeIsApplicable(DocumentTemplateType documentTemplateType) {
    assertThat(documentTemplateType.isApplicableToTerminalApplications()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "TERMINAL_FLARE_CONSENT", "TERMINAL_VENT_CONSENT" },
      mode = EnumSource.Mode.EXCLUDE
  )
  void isApplicableToTerminalApplications_documentTemplateTypeIsNotApplicable(DocumentTemplateType documentTemplateType) {
    assertThat(documentTemplateType.isApplicableToTerminalApplications()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = {
          "FIELD_PRODUCTION_CONSENT",
          "FIELD_FLARE_CONSENT",
          "TERMINAL_FLARE_CONSENT",
          "FIELD_VENT_CONSENT",
          "TERMINAL_VENT_CONSENT"
      },
      mode = EnumSource.Mode.INCLUDE
  )
  void isConsent_documentTemplateTypeIsConsent(DocumentTemplateType documentTemplateType) {
    assertThat(documentTemplateType.isConsent()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = {
          "FIELD_PRODUCTION_CONSENT",
          "FIELD_FLARE_CONSENT",
          "TERMINAL_FLARE_CONSENT",
          "FIELD_VENT_CONSENT",
          "TERMINAL_VENT_CONSENT"
      },
      mode = EnumSource.Mode.EXCLUDE
  )
  void isConsent_documentTemplateTypeIsNotConsent(DocumentTemplateType documentTemplateType) {
    assertThat(documentTemplateType.isConsent()).isFalse();
  }
}

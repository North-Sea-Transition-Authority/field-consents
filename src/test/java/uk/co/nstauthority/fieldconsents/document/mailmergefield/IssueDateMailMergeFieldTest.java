package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

class IssueDateMailMergeFieldTest {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private IssueDateMailMergeField issueDateMailMergeField;

  @BeforeEach
  void beforeEach() {
    issueDateMailMergeField = new IssueDateMailMergeField(clock);
  }

  @Test
  void getMnemonic() {
    assertThat(issueDateMailMergeField.getMnemonic()).isEqualTo(IssueDateMailMergeField.MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(issueDateMailMergeField.getDescription()).isEqualTo(IssueDateMailMergeField.DESCRIPTION);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(issueDateMailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @Test
  void resolve() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    assertThat(issueDateMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(DateUtils.format(clock.instant(), DateUtils.LONG_DATE)));
  }
}

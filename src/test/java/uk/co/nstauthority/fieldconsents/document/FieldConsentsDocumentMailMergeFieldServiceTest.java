package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeFieldService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentMailMergeFieldServiceTest {

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  private FieldConsentsDocumentMailMergeFieldService fieldConsentsDocumentMailMergeFieldService;

  @Test
  void getApplicableDocumentMailMergeFieldMnemonics() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var applicableMailMergeField = DocumentMailMergeFieldTestUtil.builder()
        .withMnemonic("TEST_MNEMONIC_1")
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto))
        .thenReturn(List.of(applicableMailMergeField));

    assertThat(
        fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto)
    ).containsExactly(
        DocumentMailMergeFieldView.from(applicableMailMergeField)
    );
  }
}

package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentMailMergeFieldControllerHelperServiceTest {

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  private DocumentMailMergeFieldControllerHelperService documentMailMergeFieldControllerHelperService;

  @Test
  void getApplicableDocumentMailMergeFieldMnemonics() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var applicableMailMergeField = DocumentMailMergeFieldTestUtil.builder()
        .withMnemonic("TEST_MNEMONIC_1")
        .build();

    when(documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto))
        .thenReturn(List.of(applicableMailMergeField));

    assertThat(documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .containsExactly(DocumentMailMergeFieldView.from(applicableMailMergeField));
  }
}

package uk.co.nstauthority.fieldconsents.document.lib;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateSectionConditionControllerHelperServiceTest {

  @Mock
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @InjectMocks
  private DocumentTemplateSectionConditionControllerHelperService documentTemplateSectionConditionControllerHelperService;

  @Test
  void getConditionsFdsSelectMap() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSectionCondition1 = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic("TEST_MNEMONIC_1")
        .withTitle("Test title 1")
        .build();
    var documentTemplateSectionCondition2 = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic("TEST_MNEMONIC_2")
        .withTitle("Test title 2")
        .build();

    when(documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditions(documentTemplateDto))
        .thenReturn(List.of(documentTemplateSectionCondition1, documentTemplateSectionCondition2));

    assertThat(documentTemplateSectionConditionControllerHelperService.getConditionsFdsSelectMap(documentTemplateDto))
        .containsOnly(
            entry(documentTemplateSectionCondition1.getMnemonic(), documentTemplateSectionCondition1.getTitle()),
            entry(documentTemplateSectionCondition2.getMnemonic(), documentTemplateSectionCondition2.getTitle())
        );
  }
}

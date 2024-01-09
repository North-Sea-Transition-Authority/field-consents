package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateSectionConditionServiceTest {

  @Mock
  private List<DocumentTemplateSectionCondition> documentTemplateSectionConditions;

  @InjectMocks
  @Spy
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Test
  void getDocumentTemplateSectionConditions() {
    assertThat(documentTemplateSectionConditionService.getDocumentTemplateSectionConditions())
        .isEqualTo(documentTemplateSectionConditions);
  }

  @Test
  void getDocumentTemplateSectionConditionOrThrow_documentTemplateSectionConditionDoesNotExist() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    doReturn(Optional.empty())
        .when(documentTemplateSectionConditionService)
        .getDocumentTemplateSectionCondition(conditionMnemonic);

    assertThatThrownBy(
        () -> documentTemplateSectionConditionService.getDocumentTemplateSectionConditionOrThrow(conditionMnemonic)
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void getDocumentTemplateSectionConditionOrThrow_documentTemplateSectionConditionExists() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var condition = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic(conditionMnemonic)
        .build();

    doReturn(Optional.of(condition))
        .when(documentTemplateSectionConditionService)
        .getDocumentTemplateSectionCondition(conditionMnemonic);

    assertThat(documentTemplateSectionConditionService.getDocumentTemplateSectionConditionOrThrow(conditionMnemonic))
        .isEqualTo(condition);
  }

  @Test
  void getDocumentTemplateSectionCondition_documentTemplateSectionConditionDoesNotExist() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    assertThat(documentTemplateSectionConditionService.getDocumentTemplateSectionCondition(conditionMnemonic)).isEmpty();
  }

  @Test
  void getDocumentTemplateSectionCondition_documentTemplateSectionConditionExists() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var condition1 = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic("OTHER_CONDITION_MNEMONIC")
        .build();
    var condition2 = DocumentTemplateSectionConditionTestUtil.builder()
        .withMnemonic(conditionMnemonic)
        .build();

    when(documentTemplateSectionConditions.stream()).thenReturn(Stream.of(condition1, condition2));

    assertThat(documentTemplateSectionConditionService.getDocumentTemplateSectionCondition(conditionMnemonic))
        .contains(condition2);
  }
}

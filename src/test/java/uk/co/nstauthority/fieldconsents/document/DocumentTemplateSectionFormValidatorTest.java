package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeFieldService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeValidationResult;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionConditionService;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateSectionFormValidatorTest {

  @Mock
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  private DocumentTemplateSectionFormValidator documentTemplateSectionFormValidator;

  @ParameterizedTest
  @NullAndEmptySource
  void validate_nullOrEmptyTitle(String title) {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withTitle(title)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.content()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("title", "title.required", "Enter a title")
        );
  }

  @Test
  void validate_nullConditionMnemonic() {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withConditionMnemonic(null)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.content()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors()).isEmpty();
  }

  @Test
  void validate_nonNullConditionMnemonicAndConditionDoesNotExist() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withConditionMnemonic(conditionMnemonic)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
            documentTemplateDto,
            conditionMnemonic
        )
    ).thenReturn(Optional.empty());
    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.content()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("conditionMnemonic", "conditionMnemonic.invalid", "Select a valid condition")
        );
  }

  @Test
  void validate_nonNullConditionMnemonicAndConditionExists() {
    var conditionMnemonic = "TEST_CONDITION_MNEMONIC";

    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withConditionMnemonic(conditionMnemonic)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    var documentTemplateSectionCondition = DocumentTemplateSectionConditionTestUtil.builder().build();

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
            documentTemplateDto,
            conditionMnemonic
        )
    ).thenReturn(Optional.of(documentTemplateSectionCondition));
    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.content()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors()).isEmpty();
  }

  @Test
  void validate_contentHasMailMergeValidationErrors() {
    var form = DocumentTemplateSectionFormTestUtil.builder().build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    var mailMergeErrorMessage = "Test error message";

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.content()))
        .thenReturn(DocumentMailMergeValidationResult.invalid(mailMergeErrorMessage));

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("content", "content.invalid", mailMergeErrorMessage)
        );
  }

  @Test
  void validate_numberedNull() {
    var form = DocumentTemplateSectionFormTestUtil.builder()
        .withNumbered(null)
        .build();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, form.content()))
        .thenReturn(DocumentMailMergeValidationResult.valid());

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, errors);

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple("numbered", "numbered.required", "Select if this section should be numbered")
        );
  }
}

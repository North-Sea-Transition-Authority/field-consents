package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class DocumentInstanceSectionFormValidatorTest {

  @Mock
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @InjectMocks
  private DocumentInstanceSectionFormValidator documentInstanceSectionFormValidator;

  @ParameterizedTest
  @NullAndEmptySource
  void validate_nullOrEmptyTitle(String title) {
    var form = DocumentInstanceSectionFormTestUtil.builder()
        .withTitle(title)
        .build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    when(
        documentMailMergeFieldService.validateMailMergeFields(
            documentInstanceDto.documentTemplateDto(),
            form.content()
        )
    ).thenReturn(DocumentMailMergeValidationResult.valid());

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

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
  void validate_contentHasMailMergeValidationError() {
    var form = DocumentInstanceSectionFormTestUtil.builder().build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var errors = new BeanPropertyBindingResult(form, "form");

    var mailMergeErrorMessage = "Test error message";

    when(
        documentMailMergeFieldService.validateMailMergeFields(
                documentInstanceDto.documentTemplateDto(),
                form.content()
            )
    ).thenReturn(DocumentMailMergeValidationResult.invalid(mailMergeErrorMessage));

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, errors);

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
}

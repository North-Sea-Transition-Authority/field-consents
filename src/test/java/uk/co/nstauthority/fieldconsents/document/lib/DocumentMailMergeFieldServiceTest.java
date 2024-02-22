package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@ExtendWith(MockitoExtension.class)
class DocumentMailMergeFieldServiceTest {

  @Mock
  private List<DocumentMailMergeField> documentMailMergeFields;

  @InjectMocks
  @Spy
  private DocumentMailMergeFieldService documentMailMergeFieldService;

  @Test
  void getApplicableDocumentMailMergeFields() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField2 = mock(DocumentMailMergeField.class);
    var documentMailMergeField3 = mock(DocumentMailMergeField.class);

    when(documentMailMergeField1.isApplicable(documentTemplateDto)).thenReturn(true);
    when(documentMailMergeField2.isApplicable(documentTemplateDto)).thenReturn(true);
    when(documentMailMergeField3.isApplicable(documentTemplateDto)).thenReturn(false);

    when(documentMailMergeFields.stream())
        .thenReturn(Stream.of(documentMailMergeField1, documentMailMergeField2, documentMailMergeField3));

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto)).containsExactly(
        documentMailMergeField1,
        documentMailMergeField2
    );
  }

  @Test
  void validateMailMergeFields_allMailMergeFieldsValid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        (((MAIL_MERGE_FIELD_2)))
        ((((MAIL_MERGE_FIELD_2))))
        (((((MAIL_MERGE_FIELD_2)))))
        (Example text in brackets)
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text))
        .isEqualTo(DocumentMailMergeValidationResult.valid());
  }

  @Test
  void validateMailMergeFields_singleMailMergeFieldInvalid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    var expectedErrorMessage =
        DocumentMailMergeFieldService.SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted("MAIL_MERGE_FIELD_2");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void validateMailMergeFields_singleMailMergeFieldInvalidAndUsedMultipleTimes() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        ((MAIL_MERGE_FIELD_2))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    var expectedErrorMessage =
        DocumentMailMergeFieldService.SINGLE_INVALID_MAIL_MERGE_FIELD_ERROR_MESSAGE.formatted("MAIL_MERGE_FIELD_2");

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void validateMailMergeFields_twoMailMergeFieldsInvalid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        ((MAIL_MERGE_FIELD_3))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_3");

    var expectedErrorMessage = DocumentMailMergeFieldService.MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE.formatted(
        StringUtil.formatStringList(List.of("MAIL_MERGE_FIELD_2", "MAIL_MERGE_FIELD_3"))
    );

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void validateMailMergeFields_threeMailMergeFieldsInvalid() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var text = """
        Example text
        
        ((MAIL_MERGE_FIELD_1))
        ((MAIL_MERGE_FIELD_2))
        ((MAIL_MERGE_FIELD_3))
        ((MAIL_MERGE_FIELD_4))
        """;

    doReturn(Optional.of(DocumentMailMergeFieldTestUtil.builder().build()))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_3");

    var expectedErrorMessage = DocumentMailMergeFieldService.MULTIPLE_INVALID_MAIL_MERGE_FIELDS_ERROR_MESSAGE.formatted(
        StringUtil.formatStringList(List.of("MAIL_MERGE_FIELD_2", "MAIL_MERGE_FIELD_3", "MAIL_MERGE_FIELD_4"))
    );

    assertThat(documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, text))
        .isEqualTo(DocumentMailMergeValidationResult.invalid(expectedErrorMessage));
  }

  @Test
  void resolveMailMergeFields_allMailMergeFieldsValid() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            (((MAIL_MERGE_FIELD_2)))
            ((((MAIL_MERGE_FIELD_2))))
            (((((MAIL_MERGE_FIELD_2)))))
            (Example text in brackets)
            """
        )
        .build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);
    var documentMailMergeField2 = mock(DocumentMailMergeField.class);

    doReturn(Optional.of(documentMailMergeField1))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.of(documentMailMergeField2))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    when(documentMailMergeField1.resolve(documentInstanceDto)).thenReturn("Resolved mail merge field 1");
    when(documentMailMergeField2.resolve(documentInstanceDto)).thenReturn("Resolved mail merge field 2");

    assertThat(documentMailMergeFieldService.resolveMailMergeFields(documentInstanceSectionDto)).isEqualTo(
        """
        Example text
        
        Resolved mail merge field 1
        Resolved mail merge field 2
        (Resolved mail merge field 2)
        ((Resolved mail merge field 2))
        (((Resolved mail merge field 2)))
        (Example text in brackets)
        """
    );
  }

  @Test
  void resolveMailMergeFields_invalidMailMergeField() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withContent(
            """
            Example text
            
            ((MAIL_MERGE_FIELD_1))
            ((MAIL_MERGE_FIELD_2))
            """
        )
        .build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    var documentMailMergeField1 = mock(DocumentMailMergeField.class);

    doReturn(Optional.of(documentMailMergeField1))
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_1");
    doReturn(Optional.empty())
        .when(documentMailMergeFieldService)
        .getApplicableDocumentMailMergeField(documentTemplateDto, "MAIL_MERGE_FIELD_2");

    when(documentMailMergeField1.resolve(documentInstanceDto)).thenReturn("Resolved mail merge field 1");

    assertThat(documentMailMergeFieldService.resolveMailMergeFields(documentInstanceSectionDto)).isEqualTo(
        """
        Example text
        
        Resolved mail merge field 1
        ((MAIL_MERGE_FIELD_2))
        """
    );
  }

  @Test
  void getApplicableDocumentMailMergeField_mailMergeFieldNotFound() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentMailMergeField = mock(DocumentMailMergeField.class);

    when(documentMailMergeFields.stream()).thenReturn(Stream.of(documentMailMergeField));

    when(documentMailMergeField.getMnemonic()).thenReturn("OTHER_MNEMONIC");

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic)).isEmpty();
  }

  @Test
  void getApplicableDocumentMailMergeField_mailMergeFieldFoundAndIsNotApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentMailMergeField = mock(DocumentMailMergeField.class);

    when(documentMailMergeFields.stream()).thenReturn(Stream.of(documentMailMergeField));

    when(documentMailMergeField.getMnemonic()).thenReturn(mnemonic);
    when(documentMailMergeField.isApplicable(documentTemplateDto)).thenReturn(false);

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic)).isEmpty();
  }

  @Test
  void getApplicableDocumentMailMergeField_mailMergeFieldFoundAndIsApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var mnemonic = "TEST_MNEMONIC";

    var documentMailMergeField = mock(DocumentMailMergeField.class);

    when(documentMailMergeFields.stream()).thenReturn(Stream.of(documentMailMergeField));

    when(documentMailMergeField.getMnemonic()).thenReturn(mnemonic);
    when(documentMailMergeField.isApplicable(documentTemplateDto)).thenReturn(true);

    assertThat(documentMailMergeFieldService.getApplicableDocumentMailMergeField(documentTemplateDto, mnemonic))
        .contains(documentMailMergeField);
  }
}

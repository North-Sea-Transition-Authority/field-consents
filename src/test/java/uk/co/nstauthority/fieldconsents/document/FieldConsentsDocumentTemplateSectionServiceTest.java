package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionConditionService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentTemplateSectionServiceTest {

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @Mock
  private DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @InjectMocks
  @Spy
  private FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService;

  @Test
  void getDocumentTemplateSectionSummaryViews() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var topLevelDocumentTemplateSectionDtos = List.of(DocumentTemplateSectionDtoTestUtil.builder().build());

    var documentTemplateSectionSummaryViewsForSectionSiblings =
        List.of(new DocumentTemplateSectionSummaryView(null, null, null, null, null, null, null, null, null));

    when(documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto))
        .thenReturn(topLevelDocumentTemplateSectionDtos);

    doReturn(documentTemplateSectionSummaryViewsForSectionSiblings)
        .when(fieldConsentsDocumentTemplateSectionService)
        .getDocumentTemplateSectionSummaryViewsForSectionSiblings(null, topLevelDocumentTemplateSectionDtos);

    assertThat(fieldConsentsDocumentTemplateSectionService.getDocumentTemplateSectionSummaryViews(documentTemplateDto))
        .isEqualTo(documentTemplateSectionSummaryViewsForSectionSiblings);
  }

  @Test
  void getDocumentTemplateSectionSummaryViewsForSectionSiblings() {
    var parentSectionNumberString = "1";

    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var conditionMnemonic1 = "TEST_CONDITION_MNEMONIC_1";
    var conditionMnemonic2 = "TEST_CONDITION_MNEMONIC_2";

    var condition1 = DocumentTemplateSectionConditionTestUtil.builder().withTitle("Test title 1").build();
    var condition2 = DocumentTemplateSectionConditionTestUtil.builder().withTitle("Test title 2").build();

    var siblingDocumentTemplateSectionDto1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(1)
            .build();

    var siblingDocumentTemplateSectionDto2Child1Child1 = DocumentTemplateSectionDtoTestUtil.builder().build();

    var siblingDocumentTemplateSectionDto2Child1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDocumentTemplateDto(documentTemplateDto)
            .withConditionMnemonic(conditionMnemonic2)
            .withDisplayOrder(1)
            .withChildren(List.of(siblingDocumentTemplateSectionDto2Child1Child1))
            .build();
    var siblingDocumentTemplateSectionDto2Child2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withNumbered(false)
            .withDisplayOrder(2)
            .build();
    var siblingDocumentTemplateSectionDto2Child3 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDisplayOrder(3)
            .build();

    var siblingDocumentTemplateSectionDto2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDocumentTemplateDto(documentTemplateDto)
            .withConditionMnemonic(conditionMnemonic1)
            .withDisplayOrder(2)
            .withChildren(
                List.of(
                    siblingDocumentTemplateSectionDto2Child2,
                    siblingDocumentTemplateSectionDto2Child3,
                    siblingDocumentTemplateSectionDto2Child1
                )
            )
            .build();

    var siblingDocumentTemplateSectionDtos =
        List.of(siblingDocumentTemplateSectionDto1, siblingDocumentTemplateSectionDto2);

    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            conditionMnemonic1
        )
    ).thenReturn(condition1);
    when(
        documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateDto,
            conditionMnemonic2
        )
    ).thenReturn(condition2);

    assertThat(
        fieldConsentsDocumentTemplateSectionService.getDocumentTemplateSectionSummaryViewsForSectionSiblings(
            parentSectionNumberString,
            siblingDocumentTemplateSectionDtos
        )
    ).containsExactly(
        DocumentTemplateSectionSummaryView.from(
            null,
            null,
            siblingDocumentTemplateSectionDto1
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.1",
            condition1.getTitle(),
            siblingDocumentTemplateSectionDto2
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.1.1",
            condition2.getTitle(),
            siblingDocumentTemplateSectionDto2Child1
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.1.1.1",
            null,
            siblingDocumentTemplateSectionDto2Child1Child1
        ),
        DocumentTemplateSectionSummaryView.from(
            null,
            null,
            siblingDocumentTemplateSectionDto2Child2
        ),
        DocumentTemplateSectionSummaryView.from(
            "1.1.2",
            null,
            siblingDocumentTemplateSectionDto2Child3
        )
    );
  }

  @Test
  void createDocumentTemplateSection() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var parentDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var form = DocumentTemplateSectionFormTestUtil.builder().build();
    int displayOrder = 1;

    fieldConsentsDocumentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form,
        displayOrder
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form.title(),
        form.content(),
        form.conditionMnemonic(),
        form.numbered(),
        displayOrder
    );
  }

  @Test
  void editDocumentTemplateSection() {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var form = DocumentTemplateSectionFormTestUtil.builder().build();

    fieldConsentsDocumentTemplateSectionService.editDocumentTemplateSection(documentTemplateSectionDto, form);

    verify(documentTemplateSectionService).editDocumentTemplateSection(
        documentTemplateSectionDto,
        form.title(),
        form.content(),
        form.conditionMnemonic(),
        form.numbered()
    );
  }
}

package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentSectionServiceTest {

  @InjectMocks
  private DocumentSectionService documentSectionService;

  @Test
  void getSectionSummaryViewsForSectionSiblings() {
    var parentSectionNumberString = "1";

    var siblingDocumentTemplateSectionDto1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDisplayOrder(1)
            .build();

    var siblingDocumentTemplateSectionDto2Child1Child1 =
        DocumentTemplateSectionDtoTestUtil.builder().build();
    var siblingDocumentTemplateSectionDto2Child1 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withChildren(List.of(siblingDocumentTemplateSectionDto2Child1Child1))
            .withDisplayOrder(1)
            .build();
    var siblingDocumentTemplateSectionDto2Child2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withDisplayOrder(2)
            .build();
    var siblingDocumentTemplateSectionDto2 =
        DocumentTemplateSectionDtoTestUtil.builder()
            .withChildren(
                List.of(
                    siblingDocumentTemplateSectionDto2Child2,
                    siblingDocumentTemplateSectionDto2Child1
                )
            )
            .withDisplayOrder(2)
            .build();

    var siblingDocumentTemplateSectionDtos =
        List.of(siblingDocumentTemplateSectionDto1, siblingDocumentTemplateSectionDto2);

    assertThat(
        documentSectionService.getSectionSummaryViewsForSectionSiblings(
            parentSectionNumberString,
            siblingDocumentTemplateSectionDtos
        )
    ).containsExactly(
        DocumentSectionSummaryView.from(
            "1.1",
            siblingDocumentTemplateSectionDto1
        ),
        DocumentSectionSummaryView.from(
            "1.2",
            siblingDocumentTemplateSectionDto2
        ),
        DocumentSectionSummaryView.from(
            "1.2.1",
            siblingDocumentTemplateSectionDto2Child1
        ),
        DocumentSectionSummaryView.from(
            "1.2.1.1",
            siblingDocumentTemplateSectionDto2Child1Child1
        ),
        DocumentSectionSummaryView.from(
            "1.2.2",
            siblingDocumentTemplateSectionDto2Child2
        )
    );
  }
}

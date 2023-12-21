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
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceSectionServiceTest {

  @Mock
  private DocumentInstanceSectionService documentInstanceSectionService;

  @InjectMocks
  @Spy
  private FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @Test
  void getDocumentInstanceSectionSummaryViews() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var topLevelDocumentInstanceSectionDtos = List.of(DocumentInstanceSectionDtoTestUtil.builder().build());

    var documentInstanceSectionSummaryViewsForSectionSiblings =
        List.of(new DocumentInstanceSectionSummaryView(null, null, null, null, null, null, null));

    when(documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto))
        .thenReturn(topLevelDocumentInstanceSectionDtos);

    doReturn(documentInstanceSectionSummaryViewsForSectionSiblings)
        .when(fieldConsentsDocumentInstanceSectionService)
        .getDocumentInstanceSectionSummaryViewsForSectionSiblings(null, topLevelDocumentInstanceSectionDtos);

    assertThat(fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViews(documentInstanceDto))
        .isEqualTo(documentInstanceSectionSummaryViewsForSectionSiblings);
  }

  @Test
  void getDocumentInstanceSectionSummaryViewsForSectionSiblings() {
    var parentSectionNumberString = "1";

    var siblingDocumentInstanceSectionDto1 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(1)
            .build();

    var siblingDocumentInstanceSectionDto2Child1Child1 =
        DocumentInstanceSectionDtoTestUtil.builder().build();
    var siblingDocumentInstanceSectionDto2Child1 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(1)
            .withChildren(List.of(siblingDocumentInstanceSectionDto2Child1Child1))
            .build();
    var siblingDocumentInstanceSectionDto2Child2 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(2)
            .build();
    var siblingDocumentInstanceSectionDto2 =
        DocumentInstanceSectionDtoTestUtil.builder()
            .withDisplayOrder(2)
            .withChildren(
                List.of(
                    siblingDocumentInstanceSectionDto2Child2,
                    siblingDocumentInstanceSectionDto2Child1
                )
            )
            .build();

    var siblingDocumentInstanceSectionDtos =
        List.of(siblingDocumentInstanceSectionDto1, siblingDocumentInstanceSectionDto2);

    assertThat(
        fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViewsForSectionSiblings(
            parentSectionNumberString,
            siblingDocumentInstanceSectionDtos
        )
    ).containsExactly(
        DocumentInstanceSectionSummaryView.from(
            "1.1",
            siblingDocumentInstanceSectionDto1
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.2",
            siblingDocumentInstanceSectionDto2
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.2.1",
            siblingDocumentInstanceSectionDto2Child1
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.2.1.1",
            siblingDocumentInstanceSectionDto2Child1Child1
        ),
        DocumentInstanceSectionSummaryView.from(
            "1.2.2",
            siblingDocumentInstanceSectionDto2Child2
        )
    );
  }

  @Test
  void createDocumentInstanceSection() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var parentDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();
    int displayOrder = 1;

    fieldConsentsDocumentInstanceSectionService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form,
        displayOrder
    );

    verify(documentInstanceSectionService).createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form.title(),
        form.content(),
        displayOrder
    );
  }

  @Test
  void editDocumentInstanceSection() {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var form = DocumentInstanceSectionFormTestUtil.builder().build();

    fieldConsentsDocumentInstanceSectionService.editDocumentInstanceSection(documentInstanceSectionDto, form);

    verify(documentInstanceSectionService).editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content()
    );
  }
}

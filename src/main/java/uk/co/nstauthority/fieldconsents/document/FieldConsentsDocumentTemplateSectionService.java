package uk.co.nstauthority.fieldconsents.document;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentSectionNumberingUtil;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;

@Service
public class FieldConsentsDocumentTemplateSectionService {

  private final DocumentTemplateSectionService documentTemplateSectionService;

  @Autowired
  FieldConsentsDocumentTemplateSectionService(DocumentTemplateSectionService documentTemplateSectionService) {
    this.documentTemplateSectionService = documentTemplateSectionService;
  }

  List<DocumentTemplateSectionSummaryView> getDocumentTemplateSectionSummaryViews(
      DocumentTemplateDto documentTemplateDto
  ) {
    var topLevelDocumentTemplateSectionDtos =
        documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto);

    return getDocumentTemplateSectionSummaryViewsForSectionSiblings(
        null,
        topLevelDocumentTemplateSectionDtos
    );
  }

  List<DocumentTemplateSectionSummaryView> getDocumentTemplateSectionSummaryViewsForSectionSiblings(
      String parentSectionNumberString,
      List<DocumentTemplateSectionDto> siblingDocumentTemplateSectionDtos
  ) {
    var documentTemplateSectionSummaryViews = new ArrayList<DocumentTemplateSectionSummaryView>();

    var sortedSiblingDocumentTemplateSectionDtos = siblingDocumentTemplateSectionDtos.stream()
        .sorted(Comparator.comparingInt(DocumentTemplateSectionDto::displayOrder))
        .toList();

    for (var i = 0; i < sortedSiblingDocumentTemplateSectionDtos.size(); i++) {
      var documentTemplateSectionDto = sortedSiblingDocumentTemplateSectionDtos.get(i);

      var sectionNumberString = DocumentSectionNumberingUtil.getFullNumberSectionNumberString(
          parentSectionNumberString,
          i + 1
      );

      var documentTemplateSectionSummaryView =
          DocumentTemplateSectionSummaryView.from(sectionNumberString, documentTemplateSectionDto);
      documentTemplateSectionSummaryViews.add(documentTemplateSectionSummaryView);

      var childrenDocumentTemplateSectionSummaryViews = getDocumentTemplateSectionSummaryViewsForSectionSiblings(
          sectionNumberString,
          documentTemplateSectionDto.children()
      );
      documentTemplateSectionSummaryViews.addAll(childrenDocumentTemplateSectionSummaryViews);
    }

    return documentTemplateSectionSummaryViews;
  }

  void createDocumentTemplateSection(
      DocumentTemplateDto documentTemplateDto,
      @Nullable DocumentTemplateSectionDto parentDto,
      DocumentTemplateSectionForm form,
      int displayOrder
  ) {
    documentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form.title(),
        form.content(),
        displayOrder
    );
  }

  void editDocumentTemplateSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentTemplateSectionForm form
  ) {
    documentTemplateSectionService.editDocumentTemplateSection(
        documentTemplateSectionDto,
        form.title(),
        form.content()
    );
  }
}

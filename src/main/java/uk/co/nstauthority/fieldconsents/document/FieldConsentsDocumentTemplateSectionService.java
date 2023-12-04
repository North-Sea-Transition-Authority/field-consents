package uk.co.nstauthority.fieldconsents.document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionNumberingUtil;
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
        documentTemplateSectionService.getDocumentTemplateSectionDtos(documentTemplateDto);

    return getSectionSummaryViewsForSectionSiblings(null, topLevelDocumentTemplateSectionDtos);
  }

  List<DocumentTemplateSectionSummaryView> getSectionSummaryViewsForSectionSiblings(
      String parentSectionNumberString,
      List<DocumentTemplateSectionDto> siblingDocumentTemplateSectionDtos
  ) {
    var sectionSummaryViews = new ArrayList<DocumentTemplateSectionSummaryView>();

    var sortedSiblingDocumentTemplateSectionDtos = siblingDocumentTemplateSectionDtos.stream()
        .sorted(Comparator.comparingInt(DocumentTemplateSectionDto::displayOrder))
        .toList();

    for (var i = 0; i < sortedSiblingDocumentTemplateSectionDtos.size(); i++) {
      var documentTemplateSectionDto = sortedSiblingDocumentTemplateSectionDtos.get(i);

      var sectionNumberString = DocumentTemplateSectionNumberingUtil.getFullNumberSectionNumberString(
          parentSectionNumberString,
          i + 1
      );

      var documentTemplateSectionSummaryView = DocumentTemplateSectionSummaryView.from(
          sectionNumberString,
          documentTemplateSectionDto
      );
      sectionSummaryViews.add(documentTemplateSectionSummaryView);

      var childrenSectionSummaryViews = getSectionSummaryViewsForSectionSiblings(
          sectionNumberString,
          documentTemplateSectionDto.children()
      );
      sectionSummaryViews.addAll(childrenSectionSummaryViews);
    }

    return sectionSummaryViews;
  }
}

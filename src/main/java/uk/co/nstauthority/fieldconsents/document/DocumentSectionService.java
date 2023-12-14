package uk.co.nstauthority.fieldconsents.document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionNumberingUtil;

@Service
class DocumentSectionService {

  <T extends DocumentSectionDto<T>> List<DocumentSectionSummaryView> getSectionSummaryViewsForSectionSiblings(
      String parentSectionNumberString,
      List<T> siblingDocumentSectionDtos
  ) {
    var sectionSummaryViews = new ArrayList<DocumentSectionSummaryView>();

    var sortedSiblingDocumentSectionDtos = siblingDocumentSectionDtos.stream()
        .sorted(Comparator.comparingInt(DocumentSectionDto::displayOrder))
        .toList();

    for (var i = 0; i < sortedSiblingDocumentSectionDtos.size(); i++) {
      var documentSectionDto = sortedSiblingDocumentSectionDtos.get(i);

      var sectionNumberString = DocumentTemplateSectionNumberingUtil.getFullNumberSectionNumberString(
          parentSectionNumberString,
          i + 1
      );

      var documentSectionSummaryView = DocumentSectionSummaryView.from(
          sectionNumberString,
          documentSectionDto
      );
      sectionSummaryViews.add(documentSectionSummaryView);

      var childrenSectionSummaryViews = getSectionSummaryViewsForSectionSiblings(
          sectionNumberString,
          documentSectionDto.children()
      );
      sectionSummaryViews.addAll(childrenSectionSummaryViews);
    }

    return sectionSummaryViews;
  }
}

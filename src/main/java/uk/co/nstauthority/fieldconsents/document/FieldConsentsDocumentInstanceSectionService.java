package uk.co.nstauthority.fieldconsents.document;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentSectionNumberingUtil;

@Service
public class FieldConsentsDocumentInstanceSectionService {

  private final DocumentInstanceSectionService documentInstanceSectionService;

  @Autowired
  FieldConsentsDocumentInstanceSectionService(DocumentInstanceSectionService documentInstanceSectionService) {
    this.documentInstanceSectionService = documentInstanceSectionService;
  }

  List<DocumentInstanceSectionSummaryView> getDocumentInstanceSectionSummaryViews(
      DocumentInstanceDto documentInstanceDto
  ) {
    var topLevelDocumentInstanceSectionDtos =
        documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto);

    return getDocumentInstanceSectionSummaryViewsForSectionSiblings(
        null,
        topLevelDocumentInstanceSectionDtos
    );
  }

  List<DocumentInstanceSectionSummaryView> getDocumentInstanceSectionSummaryViewsForSectionSiblings(
      String parentSectionNumberString,
      List<DocumentInstanceSectionDto> siblingDocumentInstanceSectionDtos
  ) {
    var documentInstanceSectionSummaryViews = new ArrayList<DocumentInstanceSectionSummaryView>();

    var sortedSiblingDocumentInstanceSectionDtos = siblingDocumentInstanceSectionDtos.stream()
        .sorted(Comparator.comparingInt(DocumentInstanceSectionDto::displayOrder))
        .toList();

    for (var i = 0; i < sortedSiblingDocumentInstanceSectionDtos.size(); i++) {
      var documentInstanceSectionDto = sortedSiblingDocumentInstanceSectionDtos.get(i);

      var sectionNumberString = DocumentSectionNumberingUtil.getFullNumberSectionNumberString(
          parentSectionNumberString,
          i + 1
      );

      var documentInstanceSectionSummaryView =
          DocumentInstanceSectionSummaryView.from(sectionNumberString, documentInstanceSectionDto);
      documentInstanceSectionSummaryViews.add(documentInstanceSectionSummaryView);

      var childrenDocumentInstanceSectionSummaryViews = getDocumentInstanceSectionSummaryViewsForSectionSiblings(
          sectionNumberString,
          documentInstanceSectionDto.children()
      );
      documentInstanceSectionSummaryViews.addAll(childrenDocumentInstanceSectionSummaryViews);
    }

    return documentInstanceSectionSummaryViews;
  }

  void createDocumentInstanceSection(
      DocumentInstanceDto documentInstanceDto,
      @Nullable DocumentInstanceSectionDto parentDto,
      DocumentInstanceSectionForm form,
      int displayOrder
  ) {
    documentInstanceSectionService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form.title(),
        form.content(),
        displayOrder
    );
  }

  void editDocumentInstanceSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    documentInstanceSectionService.editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content()
    );
  }
}

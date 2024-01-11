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
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeFieldService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentSectionNumberingUtil;

@Service
public class FieldConsentsDocumentInstanceSectionService {

  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  @Autowired
  FieldConsentsDocumentInstanceSectionService(
      DocumentInstanceSectionService documentInstanceSectionService,
      DocumentMailMergeFieldService documentMailMergeFieldService
  ) {
    this.documentInstanceSectionService = documentInstanceSectionService;
    this.documentMailMergeFieldService = documentMailMergeFieldService;
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

    var currentSectionNumber = 0;

    for (var documentInstanceSectionDto : sortedSiblingDocumentInstanceSectionDtos) {
      String sectionNumberString;

      if (documentInstanceSectionDto.numbered()) {
        currentSectionNumber++;

        sectionNumberString = DocumentSectionNumberingUtil.getFullNumberSectionNumberString(
            parentSectionNumberString,
            currentSectionNumber
        );
      } else {
        sectionNumberString = null;
      }

      var content = documentMailMergeFieldService.resolveMailMergeFields(documentInstanceSectionDto);

      var documentInstanceSectionSummaryView =
          DocumentInstanceSectionSummaryView.from(sectionNumberString, documentInstanceSectionDto, content);
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
        form.numbered(),
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
        form.content(),
        form.numbered()
    );
  }
}

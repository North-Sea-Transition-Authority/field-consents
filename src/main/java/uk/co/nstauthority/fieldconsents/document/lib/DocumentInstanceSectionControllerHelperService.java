package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentInstanceSectionControllerHelperService {

  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  DocumentInstanceSectionControllerHelperService(
      DocumentInstanceSectionService documentInstanceSectionService,
      DocumentMailMergeFieldService documentMailMergeFieldService
  ) {
    this.documentInstanceSectionService = documentInstanceSectionService;
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  public List<DocumentInstanceSectionSummaryView> getDocumentInstanceSectionSummaryViews(
      DocumentInstanceDto documentInstanceDto,
      Class<? extends DocumentInstanceSectionController> documentInstanceSectionControllerClass
  ) {
    var topLevelDocumentInstanceSectionDtos =
        documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto);

    return getDocumentInstanceSectionSummaryViewsForSectionSiblings(
        null,
        topLevelDocumentInstanceSectionDtos,
        documentInstanceSectionControllerClass
    );
  }

  List<DocumentInstanceSectionSummaryView> getDocumentInstanceSectionSummaryViewsForSectionSiblings(
      String parentSectionNumberString,
      List<DocumentInstanceSectionDto> siblingDocumentInstanceSectionDtos,
      Class<? extends DocumentInstanceSectionController> documentInstanceSectionControllerClass
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

      var documentInstanceSectionSummaryView = DocumentInstanceSectionSummaryView.from(
          sectionNumberString,
          documentInstanceSectionDto,
          content,
          documentInstanceSectionControllerClass
      );
      documentInstanceSectionSummaryViews.add(documentInstanceSectionSummaryView);

      var childrenDocumentInstanceSectionSummaryViews = getDocumentInstanceSectionSummaryViewsForSectionSiblings(
          sectionNumberString,
          documentInstanceSectionDto.children(),
          documentInstanceSectionControllerClass
      );
      documentInstanceSectionSummaryViews.addAll(childrenDocumentInstanceSectionSummaryViews);
    }

    return documentInstanceSectionSummaryViews;
  }

  public void createDocumentInstanceSection(
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
        form.hasPageBreakBefore(),
        displayOrder
    );
  }

  public void editDocumentInstanceSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    documentInstanceSectionService.editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content(),
        form.numbered(),
        form.hasPageBreakBefore()
    );
  }
}

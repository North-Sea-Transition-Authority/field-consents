package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentTemplateSectionControllerHelperService {

  private final DocumentTemplateSectionService documentTemplateSectionService;
  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  DocumentTemplateSectionControllerHelperService(
      DocumentTemplateSectionService documentTemplateSectionService,
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService
  ) {
    this.documentTemplateSectionService = documentTemplateSectionService;
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
  }

  public List<DocumentTemplateSectionSummaryView> getDocumentTemplateSectionSummaryViews(
      DocumentTemplateDto documentTemplateDto,
      Class<? extends DocumentTemplateSectionController> documentTemplateSectionControllerClass
  ) {
    var topLevelDocumentTemplateSectionDtos =
        documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto);

    return getDocumentTemplateSectionSummaryViewsForSectionSiblings(
        null,
        topLevelDocumentTemplateSectionDtos,
        documentTemplateSectionControllerClass
    );
  }

  List<DocumentTemplateSectionSummaryView> getDocumentTemplateSectionSummaryViewsForSectionSiblings(
      String parentSectionNumberString,
      List<DocumentTemplateSectionDto> siblingDocumentTemplateSectionDtos,
      Class<? extends DocumentTemplateSectionController> documentTemplateSectionControllerClass
  ) {
    var documentTemplateSectionSummaryViews = new ArrayList<DocumentTemplateSectionSummaryView>();

    var sortedSiblingDocumentTemplateSectionDtos = siblingDocumentTemplateSectionDtos.stream()
        .sorted(Comparator.comparingInt(DocumentTemplateSectionDto::displayOrder))
        .toList();

    var currentSectionNumber = 0;

    for (var documentTemplateSectionDto : sortedSiblingDocumentTemplateSectionDtos) {
      String sectionNumberString;

      if (documentTemplateSectionDto.numbered()) {
        currentSectionNumber++;

        sectionNumberString = DocumentSectionNumberingUtil.getFullNumberSectionNumberString(
            parentSectionNumberString,
            currentSectionNumber
        );
      } else {
        sectionNumberString = null;
      }

      var conditionMnemonic = documentTemplateSectionDto.conditionMnemonic();

      String conditionTitle = null;
      if (conditionMnemonic != null) {
        conditionTitle = documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
            documentTemplateSectionDto.documentTemplateDto(),
            conditionMnemonic
        ).getTitle();
      }

      var documentTemplateSectionSummaryView = DocumentTemplateSectionSummaryView.from(
          sectionNumberString,
          conditionTitle,
          documentTemplateSectionDto,
          documentTemplateSectionControllerClass
      );
      documentTemplateSectionSummaryViews.add(documentTemplateSectionSummaryView);

      var childrenDocumentTemplateSectionSummaryViews = getDocumentTemplateSectionSummaryViewsForSectionSiblings(
          sectionNumberString,
          documentTemplateSectionDto.children(),
          documentTemplateSectionControllerClass
      );
      documentTemplateSectionSummaryViews.addAll(childrenDocumentTemplateSectionSummaryViews);
    }

    return documentTemplateSectionSummaryViews;
  }

  public void createDocumentTemplateSection(
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
        form.conditionMnemonic(),
        form.numbered(),
        form.hasPageBreakBefore(),
        displayOrder
    );
  }

  public void editDocumentTemplateSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentTemplateSectionForm form
  ) {
    documentTemplateSectionService.editDocumentTemplateSection(
        documentTemplateSectionDto,
        form.title(),
        form.content(),
        form.conditionMnemonic(),
        form.numbered(),
        form.hasPageBreakBefore()
    );
  }
}

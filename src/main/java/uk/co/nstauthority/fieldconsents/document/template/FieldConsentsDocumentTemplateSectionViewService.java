package uk.co.nstauthority.fieldconsents.document.template;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldFormatter;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionUrls;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionViewService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class FieldConsentsDocumentTemplateSectionViewService {

  private final DocumentTemplateSectionViewService documentTemplateSectionViewService;

  FieldConsentsDocumentTemplateSectionViewService(
      DocumentTemplateSectionViewService documentTemplateSectionViewService
  ) {
    this.documentTemplateSectionViewService = documentTemplateSectionViewService;
  }

  public List<DocumentTemplateSectionSummaryView> getTopLevelDocumentTemplateSectionSummaryViews(
      DocumentTemplateDto documentTemplateDto
  ) {
    return documentTemplateSectionViewService.getDocumentTemplateSectionsSummaryView(
        documentTemplateDto,
        this::getDocumentTemplateSectionUrls,
        DocumentMailMergeFieldFormatter.noOp() // TODO: FCS-901
    )
        .topLevelDocumentTemplateSectionSummaryViews();
  }

  private DocumentTemplateSectionUrls getDocumentTemplateSectionUrls(DocumentTemplateSectionDto documentTemplateSectionDto) {
    var documentTemplateSectionId = documentTemplateSectionDto.id();

    return new DocumentTemplateSectionUrls(
        ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionBefore(documentTemplateSectionId)),
        ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionAfter(documentTemplateSectionId)),
        ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSubsection(documentTemplateSectionId)),
        ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getEditDocumentTemplateSection(documentTemplateSectionId)),
        ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getRemoveDocumentTemplateSection(documentTemplateSectionId))
    );
  }
}

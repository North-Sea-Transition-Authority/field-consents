package uk.co.nstauthority.fieldconsents.document.template;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionUrls;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class FieldConsentsDocumentTemplateSectionControllerHelperService {

  private final DocumentTemplateSectionControllerHelperService documentTemplateSectionControllerHelperService;

  FieldConsentsDocumentTemplateSectionControllerHelperService(
      DocumentTemplateSectionControllerHelperService documentTemplateSectionControllerHelperService
  ) {
    this.documentTemplateSectionControllerHelperService = documentTemplateSectionControllerHelperService;
  }

  public List<DocumentTemplateSectionSummaryView> getDocumentTemplateSectionSummaryViews(
      DocumentTemplateDto documentTemplateDto
  ) {
    return documentTemplateSectionControllerHelperService.getDocumentTemplateSectionSummaryViews(
        documentTemplateDto,
        this::getDocumentTemplateSectionUrls
    );
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

package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionUrls;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldConsentsDocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class FieldConsentsDocumentInstanceSectionControllerHelperService {

  private final FieldConsentsDocumentMailMergeFieldFormatter documentMailMergeFieldFormatter;
  private final DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;

  FieldConsentsDocumentInstanceSectionControllerHelperService(
      FieldConsentsDocumentMailMergeFieldFormatter documentMailMergeFieldFormatter,
      DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService
  ) {
    this.documentMailMergeFieldFormatter = documentMailMergeFieldFormatter;
    this.documentInstanceSectionControllerHelperService = documentInstanceSectionControllerHelperService;
  }

  public DocumentInstanceSectionsSummaryView getDocumentInstanceSectionsSummaryView(
      DocumentInstanceDto documentInstanceDto,
      boolean useDocumentMailMergeFieldFormatter
  ) {
    return documentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
        documentInstanceDto,
        this::getDocumentInstanceSectionUrls,
        useDocumentMailMergeFieldFormatter ? documentMailMergeFieldFormatter : DocumentMailMergeFieldFormatter.noOp()
    );
  }

  private DocumentInstanceSectionUrls getDocumentInstanceSectionUrls(DocumentInstanceSectionDto documentInstanceSectionDto) {
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    return new DocumentInstanceSectionUrls(
        ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(documentInstanceSectionId)),
        ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(documentInstanceSectionId)),
        ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(documentInstanceSectionId)),
        ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(documentInstanceSectionId)),
        ReverseRouter.route(on(FieldConsentsDocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(documentInstanceSectionId))
    );
  }
}

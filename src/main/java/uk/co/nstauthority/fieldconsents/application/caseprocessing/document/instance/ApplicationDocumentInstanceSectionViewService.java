package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionUrls;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionViewService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldConsentsDocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class ApplicationDocumentInstanceSectionViewService {

  private final FieldConsentsDocumentMailMergeFieldFormatter fieldConsentsDocumentMailMergeFieldFormatter;
  private final DocumentInstanceSectionViewService documentInstanceSectionViewService;

  ApplicationDocumentInstanceSectionViewService(
      FieldConsentsDocumentMailMergeFieldFormatter fieldConsentsDocumentMailMergeFieldFormatter,
      DocumentInstanceSectionViewService documentInstanceSectionViewService
  ) {
    this.fieldConsentsDocumentMailMergeFieldFormatter = fieldConsentsDocumentMailMergeFieldFormatter;
    this.documentInstanceSectionViewService = documentInstanceSectionViewService;
  }

  public DocumentInstanceSectionsSummaryView getDocumentInstanceSectionsSummaryView(
      Application application,
      DocumentInstanceDto documentInstanceDto,
      boolean useDocumentMailMergeFieldFormatter
  ) {
    return documentInstanceSectionViewService.getDocumentInstanceSectionsSummaryView(
        documentInstanceDto,
        documentInstanceSectionDto -> this.getDocumentInstanceSectionUrls(application, documentInstanceSectionDto),
        useDocumentMailMergeFieldFormatter ? fieldConsentsDocumentMailMergeFieldFormatter : DocumentMailMergeFieldFormatter.noOp()
    );
  }

  private DocumentInstanceSectionUrls getDocumentInstanceSectionUrls(
      Application application,
      DocumentInstanceSectionDto documentInstanceSectionDto
  ) {
    var applicationId = application.getId();
    var documentInstanceSectionId = documentInstanceSectionDto.id();

    return new DocumentInstanceSectionUrls(
        ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(applicationId, documentInstanceSectionId)),
        ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(applicationId, documentInstanceSectionId)),
        ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(applicationId, documentInstanceSectionId)),
        ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(applicationId, documentInstanceSectionId)),
        ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(applicationId, documentInstanceSectionId))
    );
  }
}

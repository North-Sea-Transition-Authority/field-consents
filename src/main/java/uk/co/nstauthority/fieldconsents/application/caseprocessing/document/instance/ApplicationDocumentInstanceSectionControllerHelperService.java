package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionUrls;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldConsentsDocumentMailMergeFieldFormatter;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class ApplicationDocumentInstanceSectionControllerHelperService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDocumentInstanceSectionControllerHelperService.class);

  private final FieldConsentsDocumentMailMergeFieldFormatter fieldConsentsDocumentMailMergeFieldFormatter;
  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;
  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  ApplicationDocumentInstanceSectionControllerHelperService(
      FieldConsentsDocumentMailMergeFieldFormatter fieldConsentsDocumentMailMergeFieldFormatter,
      DocumentInstanceSectionService documentInstanceSectionService,
      DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService,
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService
  ) {
    this.fieldConsentsDocumentMailMergeFieldFormatter = fieldConsentsDocumentMailMergeFieldFormatter;
    this.documentInstanceSectionService = documentInstanceSectionService;
    this.documentInstanceSectionControllerHelperService = documentInstanceSectionControllerHelperService;
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
  }

  public DocumentInstanceSectionDto getDocumentInstanceSectionDtoForApplicationOrThrow(
      Application application,
      UUID documentInstanceSectionId
  ) {
    var applicationId = application.getId();

    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentInstanceApplicationId = applicationDocumentInstanceLinkingService
        .getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto);

    if (applicationId != documentInstanceApplicationId) {
      LOGGER.warn(
          "Access was attempted to document instance section {} with incorrect application {}",
          documentInstanceSectionId,
          applicationId
      );

      throw new ResponseStatusException(NOT_FOUND, "Document instance section %s does not exist for application %s"
          .formatted(documentInstanceSectionId, applicationId));
    }

    return documentInstanceSectionDto;
  }

  public DocumentInstanceSectionsSummaryView getDocumentInstanceSectionsSummaryView(
      Application application,
      DocumentInstanceDto documentInstanceDto,
      boolean useDocumentMailMergeFieldFormatter
  ) {
    return documentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
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

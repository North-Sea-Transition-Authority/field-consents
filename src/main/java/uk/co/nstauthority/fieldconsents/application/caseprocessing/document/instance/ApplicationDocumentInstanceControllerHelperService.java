package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSummaryView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class ApplicationDocumentInstanceControllerHelperService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDocumentInstanceControllerHelperService.class);

  private final ApplicationDocumentInstanceService applicationDocumentInstanceService;
  private final DocumentInstanceService documentInstanceService;
  private final DocumentInstanceControllerHelperService documentInstanceControllerHelperService;
  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  ApplicationDocumentInstanceControllerHelperService(
      ApplicationDocumentInstanceService applicationDocumentInstanceService,
      DocumentInstanceService documentInstanceService,
      DocumentInstanceControllerHelperService documentInstanceControllerHelperService,
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService
  ) {
    this.applicationDocumentInstanceService = applicationDocumentInstanceService;
    this.documentInstanceService = documentInstanceService;
    this.documentInstanceControllerHelperService = documentInstanceControllerHelperService;
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
  }

  public DocumentInstanceDto getDocumentInstanceDtoForApplicationOrThrow(Application application, UUID documentInstanceId) {
    var applicationId = application.getId();

    var documentInstanceDto = documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId);
    var documentInstanceApplicationId = applicationDocumentInstanceLinkingService
        .getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto);

    if (applicationId != documentInstanceApplicationId) {
      LOGGER.warn(
          "Access was attempted to document instance {} with incorrect application {}",
          documentInstanceId,
          applicationId
      );

      throw new ResponseStatusException(NOT_FOUND, "Document instance %s does not exist for application %s"
          .formatted(documentInstanceId, applicationId));
    }

    return documentInstanceDto;
  }

  public List<DocumentInstanceSummaryView> getDocumentInstanceSummaryViews(Application application) {
    var documentInstanceDtos = applicationDocumentInstanceService.getDocumentInstanceDtos(application);

    return documentInstanceControllerHelperService.getDocumentInstanceSummaryViews(
        documentInstanceDtos,
        documentInstanceDto -> ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(application.getId(), documentInstanceDto.id()))
    );
  }
}

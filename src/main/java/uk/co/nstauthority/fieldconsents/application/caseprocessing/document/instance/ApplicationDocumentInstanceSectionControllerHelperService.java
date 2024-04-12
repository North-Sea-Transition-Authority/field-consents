package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionService;
import uk.co.nstauthority.fieldconsents.application.Application;

@Service
public class ApplicationDocumentInstanceSectionControllerHelperService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDocumentInstanceSectionControllerHelperService.class);

  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  ApplicationDocumentInstanceSectionControllerHelperService(
      DocumentInstanceSectionService documentInstanceSectionService,
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService
  ) {
    this.documentInstanceSectionService = documentInstanceSectionService;
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
}

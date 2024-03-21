package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;

@Service
public class ApplicationDocumentInstanceLinkingService {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;

  @Autowired
  ApplicationDocumentInstanceLinkingService(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
  }

  public ApplicationVersion getLatestApplicationVersionFromDocumentInstanceDto(DocumentInstanceDto documentInstanceDto) {
    var applicationId = getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto);
    return applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
  }

  public Application getApplicationFromDocumentInstanceDto(DocumentInstanceDto documentInstanceDto) {
    var applicationId = getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto);
    return applicationService.getApplicationById(applicationId);
  }

  public int getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(DocumentInstanceDto documentInstanceDto) {
    var documentInstanceItemType = documentInstanceDto.itemType();
    if (!APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE.equals(documentInstanceItemType)) {
      throw new IllegalStateException("Expected itemType %s but found %s".formatted(
          APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
          documentInstanceItemType
      ));
    }

    return Integer.parseInt(documentInstanceDto.itemReference());
  }

}

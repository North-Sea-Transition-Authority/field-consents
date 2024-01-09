package uk.co.nstauthority.fieldconsents.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;

@Service
public class DocumentInstanceLinkingService {

  private final ApplicationVersionService applicationVersionService;

  @Autowired
  DocumentInstanceLinkingService(ApplicationVersionService applicationVersionService) {
    this.applicationVersionService = applicationVersionService;
  }

  public ApplicationVersion getApplicationVersionFromDocumentInstanceDto(DocumentInstanceDto documentInstanceDto) {
    return applicationVersionService.getApplicationVersionById(Integer.parseInt(documentInstanceDto.itemReference()));
  }
}

package uk.co.nstauthority.fieldconsents.document;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionService;

@Service
public class FieldConsentsDocumentInstanceSectionService {

  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentSectionService documentSectionService;

  @Autowired
  FieldConsentsDocumentInstanceSectionService(
      DocumentInstanceSectionService documentInstanceSectionService,
      DocumentSectionService documentSectionService
  ) {
    this.documentInstanceSectionService = documentInstanceSectionService;
    this.documentSectionService = documentSectionService;
  }

  List<DocumentSectionSummaryView> getDocumentSectionSummaryViews(
      DocumentInstanceDto documentInstanceDto
  ) {
    var topLevelDocumentInstanceSectionDtos =
        documentInstanceSectionService.getTopLevelDocumentInstanceSectionDtos(documentInstanceDto);

    return documentSectionService.getSectionSummaryViewsForSectionSiblings(
        null,
        topLevelDocumentInstanceSectionDtos
    );
  }
}

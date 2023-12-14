package uk.co.nstauthority.fieldconsents.document;

import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;
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

  void createDocumentInstanceSection(
      DocumentInstanceDto documentInstanceDto,
      @Nullable DocumentInstanceSectionDto parentDto,
      DocumentSectionForm form,
      int displayOrder
  ) {
    documentInstanceSectionService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form.title(),
        form.content(),
        displayOrder
    );
  }

  void editDocumentInstanceSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentSectionForm form
  ) {
    documentInstanceSectionService.editDocumentInstanceSection(
        documentInstanceSectionDto,
        form.title(),
        form.content()
    );
  }
}

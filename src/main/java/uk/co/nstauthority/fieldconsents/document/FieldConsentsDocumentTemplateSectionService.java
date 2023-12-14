package uk.co.nstauthority.fieldconsents.document;

import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;

@Service
public class FieldConsentsDocumentTemplateSectionService {

  private final DocumentTemplateSectionService documentTemplateSectionService;
  private final DocumentSectionService documentSectionService;

  @Autowired
  FieldConsentsDocumentTemplateSectionService(
      DocumentTemplateSectionService documentTemplateSectionService,
      DocumentSectionService documentSectionService
  ) {
    this.documentTemplateSectionService = documentTemplateSectionService;
    this.documentSectionService = documentSectionService;
  }

  List<DocumentSectionSummaryView> getDocumentSectionSummaryViews(
      DocumentTemplateDto documentTemplateDto
  ) {
    var topLevelDocumentTemplateSectionDtos =
        documentTemplateSectionService.getTopLevelDocumentTemplateSectionDtos(documentTemplateDto);

    return documentSectionService.getSectionSummaryViewsForSectionSiblings(
        null,
        topLevelDocumentTemplateSectionDtos
    );
  }

  void createDocumentTemplateSection(
      DocumentTemplateDto documentTemplateDto,
      @Nullable DocumentTemplateSectionDto parentDto,
      DocumentSectionForm form,
      int displayOrder
  ) {
    documentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form.title(),
        form.content(),
        displayOrder
    );
  }

  void editDocumentTemplateSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentSectionForm form
  ) {
    documentTemplateSectionService.editDocumentTemplateSection(
        documentTemplateSectionDto,
        form.title(),
        form.content()
    );
  }
}

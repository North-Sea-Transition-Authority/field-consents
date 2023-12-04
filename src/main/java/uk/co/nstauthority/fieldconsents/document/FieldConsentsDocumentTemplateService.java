package uk.co.nstauthority.fieldconsents.document;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;

@Service
public class FieldConsentsDocumentTemplateService {

  private final DocumentTemplateService documentTemplateService;

  @Autowired
  FieldConsentsDocumentTemplateService(DocumentTemplateService documentTemplateService) {
    this.documentTemplateService = documentTemplateService;
  }

  List<DocumentTemplateSummaryView> getDocumentTemplateSummaryViews() {
    return documentTemplateService.getDocumentTemplateDtos().stream()
        .sorted(Comparator.comparingInt(DocumentTemplateDto::displayOrder))
        .map(DocumentTemplateSummaryView::from)
        .toList();
  }
}

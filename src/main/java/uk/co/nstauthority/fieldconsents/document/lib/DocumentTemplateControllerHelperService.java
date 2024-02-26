package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentTemplateControllerHelperService {

  private final DocumentTemplateService documentTemplateService;

  DocumentTemplateControllerHelperService(DocumentTemplateService documentTemplateService) {
    this.documentTemplateService = documentTemplateService;
  }

  public List<DocumentTemplateSummaryView> getDocumentTemplateSummaryViews(
      Class<? extends DocumentTemplateController> documentTemplateControllerClass
  ) {
    return documentTemplateService.getDocumentTemplateDtos().stream()
        .sorted(Comparator.comparingInt(DocumentTemplateDto::displayOrder))
        .map(documentTemplateDto -> DocumentTemplateSummaryView.from(documentTemplateDto, documentTemplateControllerClass))
        .toList();
  }
}

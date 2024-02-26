package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentMailMergeFieldControllerHelperService {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  DocumentMailMergeFieldControllerHelperService(DocumentMailMergeFieldService documentMailMergeFieldService) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  public List<DocumentMailMergeFieldView> getApplicableDocumentMailMergeFieldViews(DocumentTemplateDto documentTemplateDto) {
    return documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto).stream()
        .map(DocumentMailMergeFieldView::from)
        .toList();
  }
}

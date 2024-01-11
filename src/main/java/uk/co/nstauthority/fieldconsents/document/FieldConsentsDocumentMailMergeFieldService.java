package uk.co.nstauthority.fieldconsents.document;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeFieldService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

@Service
class FieldConsentsDocumentMailMergeFieldService {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  @Autowired
  FieldConsentsDocumentMailMergeFieldService(DocumentMailMergeFieldService documentMailMergeFieldService) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  public List<DocumentMailMergeFieldView> getApplicableDocumentMailMergeFieldViews(
      DocumentTemplateDto documentTemplateDto
  ) {
    return documentMailMergeFieldService.getApplicableDocumentMailMergeFields(documentTemplateDto).stream()
        .sorted(Comparator.comparing(DocumentMailMergeField::getMnemonic))
        .map(DocumentMailMergeFieldView::from)
        .toList();
  }
}

package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentTemplateSectionConditionService {

  private final List<DocumentTemplateSectionCondition> documentTemplateSectionConditions;

  @Autowired
  DocumentTemplateSectionConditionService(List<DocumentTemplateSectionCondition> documentTemplateSectionConditions) {
    this.documentTemplateSectionConditions = documentTemplateSectionConditions;
  }

  public List<DocumentTemplateSectionCondition> getApplicableDocumentTemplateSectionConditions(
      DocumentTemplateDto documentTemplateDto
  ) {
    return documentTemplateSectionConditions.stream()
        .filter(documentTemplateSectionCondition -> documentTemplateSectionCondition.isApplicable(documentTemplateDto))
        .toList();
  }

  public DocumentTemplateSectionCondition getApplicableDocumentTemplateSectionConditionOrThrow(
      DocumentTemplateDto documentTemplateDto,
      String mnemonic
  ) {
    return getApplicableDocumentTemplateSectionCondition(documentTemplateDto, mnemonic).orElseThrow(() ->
        new IllegalStateException("Unable to find applicable document section condition %s".formatted(mnemonic))
    );
  }

  public Optional<DocumentTemplateSectionCondition> getApplicableDocumentTemplateSectionCondition(
      DocumentTemplateDto documentTemplateDto,
      String mnemonic
  ) {
    return documentTemplateSectionConditions.stream()
        .filter(documentTemplateSectionCondition -> documentTemplateSectionCondition.getMnemonic().equals(mnemonic))
        .filter(documentTemplateSectionCondition -> documentTemplateSectionCondition.isApplicable(documentTemplateDto))
        .findFirst();
  }
}

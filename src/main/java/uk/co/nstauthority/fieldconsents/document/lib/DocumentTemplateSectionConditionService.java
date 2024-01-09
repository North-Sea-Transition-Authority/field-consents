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

  public List<DocumentTemplateSectionCondition> getDocumentTemplateSectionConditions() {
    return documentTemplateSectionConditions;
  }

  public DocumentTemplateSectionCondition getDocumentTemplateSectionConditionOrThrow(String conditionMnemonic) {
    return getDocumentTemplateSectionCondition(conditionMnemonic).orElseThrow(() ->
        new IllegalStateException("Unable to find document section condition %s".formatted(conditionMnemonic))
    );
  }

  public Optional<DocumentTemplateSectionCondition> getDocumentTemplateSectionCondition(String conditionMnemonic) {
    return documentTemplateSectionConditions.stream()
        .filter(documentSectionCondition -> documentSectionCondition.getMnemonic().equals(conditionMnemonic))
        .findFirst();
  }
}

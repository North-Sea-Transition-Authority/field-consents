package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentTemplateSectionConditionControllerHelperService {

  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Autowired
  DocumentTemplateSectionConditionControllerHelperService(
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService
  ) {
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
  }

  public Map<String, String> getConditionsFdsSelectMap(DocumentTemplateDto documentTemplateDto) {
    return documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditions(documentTemplateDto)
        .stream()
        .collect(
            Collectors.toMap(
                DocumentTemplateSectionCondition::getMnemonic,
                DocumentTemplateSectionCondition::getTitle
            )
        );
  }
}

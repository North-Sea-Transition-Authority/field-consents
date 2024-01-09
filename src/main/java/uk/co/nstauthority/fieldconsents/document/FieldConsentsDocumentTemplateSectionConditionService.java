package uk.co.nstauthority.fieldconsents.document;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionCondition;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionConditionService;

@Service
class FieldConsentsDocumentTemplateSectionConditionService {

  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Autowired
  FieldConsentsDocumentTemplateSectionConditionService(
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService
  ) {
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
  }

  Map<String, String> getConditionsFdsSelectMap() {
    return documentTemplateSectionConditionService.getDocumentTemplateSectionConditions().stream()
        .collect(
            Collectors.toMap(
                DocumentTemplateSectionCondition::getMnemonic,
                DocumentTemplateSectionCondition::getTitle
            )
        );
  }
}

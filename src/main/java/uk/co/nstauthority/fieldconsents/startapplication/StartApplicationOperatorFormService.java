package uk.co.nstauthority.fieldconsents.startapplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

@Service
class StartApplicationOperatorFormService {

  static final String OPERATOR_SEARCH_PURPOSE = "Get preselected operator";

  private final FieldService fieldService;

  private final TerminalService terminalService;

  @Autowired
  StartApplicationOperatorFormService(FieldService fieldService, TerminalService terminalService) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  RestSearchItem getPrefilledOperatorForField(Integer fieldId) {
    return fieldService.findFieldWithOperator(fieldId, OPERATOR_SEARCH_PURPOSE)
        .map(FieldWithOperatorJson::getOperatorJson)
        .map(organisationUnitJson ->
            new RestSearchItem(organisationUnitJson.getSelectionId(), organisationUnitJson.getSelectionText()))
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  RestSearchItem getPrefilledOperatorForTerminal(Integer terminalId) {
    return terminalService.findTerminalWithOperator(terminalId, OPERATOR_SEARCH_PURPOSE)
        .map(TerminalWithOperatorJson::getOperatorJson)
        .map(organisationUnitJson ->
            new RestSearchItem(organisationUnitJson.getSelectionId(), organisationUnitJson.getSelectionText()))
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}

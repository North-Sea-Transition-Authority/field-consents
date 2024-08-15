package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@Component
class BulkCaseActionSelectionFormValidator {

  private static final String SELECT_ONE_OF_THE_AVAILABLE_ACTIONS = "Select one of the available actions";

  void validate(BulkCaseActionSelectionForm form, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "selectedAction", "required", SELECT_ONE_OF_THE_AVAILABLE_ACTIONS);
    if (errors.hasErrors()) {
      return;
    }

    try {
      BulkCaseAction.valueOf(form.selectedAction());
    } catch (RuntimeException e) {
      errors.rejectValue("selectedAction", "invalid", SELECT_ONE_OF_THE_AVAILABLE_ACTIONS);
    }
  }
}

package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
class BulkIssueConsentsFormValidator {

  private static final String REQUIRED = "required";
  private static final String SELECTED_APPLICATION_IDS = "selectedApplicationIds";
  private static final String SELECT_AT_LEAST_ONE_APPLICATION = "Select at least one application";

  void validate(BulkIssueConsentsForm form, Errors errors) {
    if (form.selectedApplicationIds() == null || form.selectedApplicationIds().isEmpty()) {
      errors.rejectValue(SELECTED_APPLICATION_IDS, REQUIRED, SELECT_AT_LEAST_ONE_APPLICATION);
      return;
    }

    try {
      for (var applicationId : form.selectedApplicationIds()) {
        Integer.parseInt(applicationId);
      }
    } catch (NumberFormatException e) {
      errors.rejectValue(SELECTED_APPLICATION_IDS, REQUIRED, SELECT_AT_LEAST_ONE_APPLICATION);
    }
  }

}

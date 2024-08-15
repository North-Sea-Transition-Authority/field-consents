package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Component
class BulkAssignCaseOfficerFormValidator {

  private static final String REQUIRED = "required";

  private static final String CASE_OFFICER_WUA_ID = "caseOfficerWuaId";
  private static final String SELECTED_APPLICATION_IDS = "selectedApplicationIds";

  private static final String SELECT_A_CASE_OFFICER = "Select a case officer";
  private static final String SELECT_AT_LEAST_ONE_APPLICATION = "Select at least one application";

  private final RegulatorTeamService regulatorTeamService;
  private final EnergyPortalUserService energyPortalUserService;

  BulkAssignCaseOfficerFormValidator(
      RegulatorTeamService regulatorTeamService,
      EnergyPortalUserService energyPortalUserService
  ) {
    this.regulatorTeamService = regulatorTeamService;
    this.energyPortalUserService = energyPortalUserService;
  }

  void validate(BulkAssignCaseOfficerForm form, Errors errors) {
    validateCaseOfficerWuaId(form, errors);
    validateSelectedApplicationIds(form, errors);
  }

  void validateCaseOfficerWuaId(BulkAssignCaseOfficerForm form, Errors errors) {
    try {
      var webUserAccountId = WebUserAccountId.valueOf(form.caseOfficerWuaId());
      if (!regulatorTeamService.isCaseOfficer(webUserAccountId)) {
        errors.rejectValue(CASE_OFFICER_WUA_ID, REQUIRED, SELECT_A_CASE_OFFICER);
      }

      if (errors.hasFieldErrors(CASE_OFFICER_WUA_ID)) {
        return;
      }

      var energyPortalUserExists = energyPortalUserService.findByWuaId(webUserAccountId).isPresent();
      if (!energyPortalUserExists) {
        errors.rejectValue(CASE_OFFICER_WUA_ID, REQUIRED, SELECT_A_CASE_OFFICER);
      }
    } catch (NumberFormatException e) {
      errors.rejectValue(CASE_OFFICER_WUA_ID, REQUIRED, SELECT_A_CASE_OFFICER);
    }
  }

  void validateSelectedApplicationIds(BulkAssignCaseOfficerForm form, Errors errors) {
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

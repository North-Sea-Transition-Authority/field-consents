package uk.co.nstauthority.fieldconsents.teams.management.form;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementService;

@Service
public class NewOrganisationTeamFormValidator {

  private final TeamManagementService teamManagementService;

  NewOrganisationTeamFormValidator(TeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  public boolean isValid(NewOrganisationTeamForm form, Errors errors) {
    if (form.getOrgGroupId() == null || form.getOrgGroupId().isEmpty()) {
      errors.rejectValue("orgGroupId", "required", "Select an organisation");
      return false;
    }

    var scopedTeamWithReferenceExists = teamManagementService.doesScopedTeamWithReferenceExist(
        TeamType.INDUSTRY,
        TeamScopeReference.from(form.getOrgGroupId(), TeamScopeReference.ORGANISATION_GROUP_ID)
    );

    if (scopedTeamWithReferenceExists) {
      errors.rejectValue("orgGroupId", "alreadyExists", "A team for this organisation already exists");
    }

    return !errors.hasErrors();
  }

}

package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;

@Service
public class OpredNewTeamFormValidator implements Validator {

  private static final String ORGANISATION_GROUP_ID_VALUE_PATH = "organisationGroupId";

  private final OrganisationGroupQueryService organisationGroupQueryService;

  OpredNewTeamFormValidator(OrganisationGroupQueryService organisationGroupQueryService) {
    this.organisationGroupQueryService = organisationGroupQueryService;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return OpredNewTeamForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (OpredNewTeamForm) target;

    ValidationUtils.rejectIfEmpty(errors,
        ORGANISATION_GROUP_ID_VALUE_PATH,
        "organisationGroupId.required",
        "Select an organisation group to create a team for");
    if (errors.hasFieldErrors(ORGANISATION_GROUP_ID_VALUE_PATH)) {
      return;
    }

    var orgGroupId = Integer.parseInt(form.getOrganisationGroupId());
    if (organisationGroupQueryService.getOrganisationGroupById(orgGroupId).isEmpty()) {
      errors.rejectValue(
          ORGANISATION_GROUP_ID_VALUE_PATH,
          "organisationGroupId.doesNotExist",
          "The selected organisation group does not exist");
    }
  }
}

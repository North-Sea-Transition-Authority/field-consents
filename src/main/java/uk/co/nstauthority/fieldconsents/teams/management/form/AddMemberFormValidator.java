package uk.co.nstauthority.fieldconsents.teams.management.form;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementService;

@Service
public class AddMemberFormValidator {

  private static final String FIELD_NAME = "username";

  private final TeamManagementService teamManagementService;

  AddMemberFormValidator(TeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  public boolean isValid(AddMemberForm form, Errors errors) {
    if (StringUtils.isBlank(form.getUsername())) {
      errors.rejectValue(
          FIELD_NAME,
          "required",
          "Enter an Energy Portal username"
      );
      return false;
    }

    var users = teamManagementService.getEnergyPortalUser(form.getUsername());
    if (users.isEmpty()) {
      errors.rejectValue(
          FIELD_NAME,
          "notFound",
          "No Energy Portal user exists with this username"
      );
      return false;
    }

    if (users.size() > 1) {
      errors.rejectValue(
          FIELD_NAME,
          "tooMany",
          "More than one Energy Portal user exists with this email address. Enter the username of the user instead."
      );
    }

    var user = users.getFirst();
    var isAccountShared = Optional.ofNullable(user.getIsAccountShared())
        .orElseThrow(() -> new IllegalStateException(
            "Unable to determine if user [%s] is shared".formatted(user.getWebUserAccountId())
        ));

    if (Boolean.TRUE.equals(isAccountShared)) {
      errors.rejectValue(
          FIELD_NAME,
          "sharedAccount",
          "You cannot add shared accounts to this service"
      );
    }

    var canLogin = Optional.ofNullable(user.getCanLogin())
        .orElseThrow(() -> new IllegalStateException(
            "Unable to determine if user [%s] can log in".formatted(user.getWebUserAccountId())
        ));

    if (!Boolean.TRUE.equals(canLogin)) {
      errors.rejectValue(
          FIELD_NAME,
          "inactiveAccount",
          "This user does not have login access to the Energy Portal and can't be added to this service"
      );
    }

    return !errors.hasErrors();
  }
}

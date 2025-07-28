package uk.co.nstauthority.fieldconsents.teams.management.form;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class AddMemberFormValidator {

  private static final String FIELD_NAME = "emailAddress";

  private final EnergyPortalUserService energyPortalUserService;

  AddMemberFormValidator(EnergyPortalUserService energyPortalUserService) {
    this.energyPortalUserService = energyPortalUserService;
  }

  public boolean isValid(AddMemberForm form, Errors errors) {
    if (StringUtils.isBlank(form.getEmailAddress())) {
      errors.rejectValue(
          FIELD_NAME,
          "required",
          "Enter a UK Energy Portal email address"
      );
      return false;
    }

    var users = energyPortalUserService.getEnergyPortalUsersThatCanLogin(form.getEmailAddress());
    if (users.isEmpty()) {
      errors.rejectValue(
          FIELD_NAME,
          "notFound",
          "No UK Energy Portal account exists with this email address"
      );
      return false;
    }

    var user = users.get();

    var canLogin = Optional.ofNullable(user.getCanLogin())
        .orElseThrow(() -> new IllegalStateException(
            "Unable to determine if user [%s] can log in".formatted(user.getWebUserAccountId())
        ));

    if (!Boolean.TRUE.equals(canLogin)) {
      errors.rejectValue(
          FIELD_NAME,
          "inactiveAccount",
          "This user does not have login access to the UK Energy Portal and can't be added to this service"
      );
    }

    return !errors.hasErrors();
  }
}

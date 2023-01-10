package uk.co.nstauthority.fieldconsents.startapplication;

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.integer.IntegerInputValidator;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
class StartApplicationOperatorFormValidator implements Validator {

  private final OrganisationUnitService organisationUnitService;

  @Autowired
  StartApplicationOperatorFormValidator(OrganisationUnitService organisationUnitService) {
    this.organisationUnitService = organisationUnitService;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return StartApplicationOperatorForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (StartApplicationOperatorForm) target;
    var purpose = "Check organisation unit exists when starting an application";

    IntegerInputValidator.builder()
        .mustBeMoreThanOrEqual(0)
        .validate(form.getOrganisationUnitId(), errors);

    if (!errors.hasErrors() && organisationUnitService
        .findOrganisationUnitById(
            form.getOrganisationUnitId().getAsInteger().orElseThrow(NoSuchElementException::new),
            purpose
        ).isEmpty()) {
      errors.rejectValue(
          "organisationUnitId.inputValue",
          "organisationUnitId.doesNotExist",
          "That operator does not exist");
    }
  }
}

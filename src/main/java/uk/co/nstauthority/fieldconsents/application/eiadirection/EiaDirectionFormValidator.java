package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.time.LocalDate;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;

@Service
public class EiaDirectionFormValidator implements Validator {

  private final PetsApplicationService petsApplicationService;

  static final String HAVE_SUBMITTED_EIA_DIRECTION_EMPTY =
      "Select yes if you have submitted an EIA screening direction";

  static final String EIA_DIRECTION_REF_EMPTY = "Select an EIA screening direction reference";

  static final String EIA_DIRECTION_REF_INVALID = "That EIA screening direction does not exist";

  static final String HAVE_EIA_DIRECTION_TO_SUBMIT_EMPTY =
      "Select yes if you have an EIA screening direction to submit";

  static final String SUBMIT_DATE_NOT_IN_FUTURE = "Submission date must be in the future";

  @Autowired
  public EiaDirectionFormValidator(PetsApplicationService petsApplicationService) {
    this.petsApplicationService = petsApplicationService;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return EiaDirectionForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    EiaDirectionForm form = (EiaDirectionForm) target;

    ValidationUtils.rejectIfEmpty(errors,
        "haveSubmittedEiaDirection",
        "haveSubmittedEiaDirection.required", HAVE_SUBMITTED_EIA_DIRECTION_EMPTY);

    if (errors.hasErrors()) {
      return;
    }

    if (Boolean.TRUE.equals(form.getHaveSubmittedEiaDirection())) {
      ValidationUtils.rejectIfEmpty(errors, "satId", "satId.required", EIA_DIRECTION_REF_EMPTY);

      var purpose = "Check pets application exists when saving the EIA screening direction form page";
      // check that the EIA direction exists
      if (!errors.hasErrors()
          && petsApplicationService.findPetsApplicationById(form.getSatId(), purpose).isEmpty()) {
        errors.rejectValue("satId", "satId.doesNotExist", EIA_DIRECTION_REF_INVALID);
      }
    } else {
      ValidationUtils.rejectIfEmpty(errors,
          "haveEiaDirectionToSubmit",
          "haveEiaDirectionToSubmit.required", HAVE_EIA_DIRECTION_TO_SUBMIT_EMPTY);

      if (errors.hasErrors()) {
        return;
      }

      if (Boolean.TRUE.equals(form.getHaveEiaDirectionToSubmit())) {
        ThreeFieldDateInputValidator.builder()
            .mustBeAfterDate(LocalDate.now())
            .mustBeAfterDateErrorMessage(SUBMIT_DATE_NOT_IN_FUTURE)
            .validate(form.getLatestDateToBeSubmitted(), errors);
      } else {
        StringInputValidator.builder()
            .validate(form.getWhyNoEiaDirection(), errors);
      }
    }
  }
}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.decimal.DecimalInputValidator;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresInputValidator;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Component
class ConsentDataFormValidator {

  private final ConsentProductionFiguresInputValidator consentProductionFiguresInputValidator;

  ConsentDataFormValidator(ConsentProductionFiguresInputValidator consentProductionFiguresInputValidator) {
    this.consentProductionFiguresInputValidator = consentProductionFiguresInputValidator;
  }

  public void validate(ConsentDataForm form, Application application, ConsentLengthType consentLengthType, Errors errors) {
    var consentStartDateInput = form.getConsentStartDateInput();
    var consentStartDateOptional = consentStartDateInput.getAsLocalDate();

    ThreeFieldDateInputValidator.builder().validate(consentStartDateInput, errors);

    if (!errors.hasErrors()) {
      var consentStartDate = consentStartDateOptional.orElseThrow();

      ThreeFieldDateInputValidator.builder()
          .mustBeAfterOrEqualTo(consentStartDate)
          .mustBeAfterOrEqualToErrorMessage("Consent end date must be on or after the consent start date")
          .validate(form.getConsentEndDateInput(), errors);
    }

    var applicationType = application.getType();
    if (applicationType == ApplicationType.PRODUCTION) {
      if (consentLengthType == ConsentLengthType.SHORT_TERM || consentLengthType == ConsentLengthType.ANNUAL) {
        ValidatorUtils.invokeNestedValidator(
            errors,
            consentProductionFiguresInputValidator,
            "shortTermOrAnnualConsentProductionFiguresInput",
            form.getShortTermOrAnnualConsentProductionFiguresInput(),
            errors
        );
      } else if (consentLengthType == ConsentLengthType.LONG_TERM) {
        var consentEndDateInput = form.getConsentEndDateInput();

        if (!consentStartDateInput.fieldHasErrors(errors) && !consentEndDateInput.fieldHasErrors(errors)) {
          ThreeFieldDateInputValidator.builder()
              .mustBeAfterOrEqualTo(consentStartDateInput.getAsLocalDate().orElseThrow())
              .mustBeAfterOrEqualToErrorMessage("Consent schedule start date must be on or after the consent start date")
              .mustBeBeforeOrEqualTo(consentEndDateInput.getAsLocalDate().orElseThrow())
              .mustBeBeforeOrEqualToErrorMessage("Consent schedule start date must be on or before the consent end date")
              .validate(form.getLongTermProductionConsentScheduleStartDateInput(), errors);
        }

        form.getLongTermConsentProductionFiguresInputs().forEach((year, consentProductionFiguresInput) ->
            ValidatorUtils.invokeNestedValidator(
                errors,
                consentProductionFiguresInputValidator,
                "longTermConsentProductionFiguresInputs[%s]".formatted(year),
                consentProductionFiguresInput,
                errors
            )
        );
      }
    } else if (applicationType == ApplicationType.FLARE || applicationType == ApplicationType.VENT) {
      DecimalInputValidator.builder()
          .mustBeMoreThanOrEqualTo(BigDecimal.ZERO)
          .mustHaveNoMoreThanDecimalPlaces(ValidatorUtils.MAX_DECIMAL_PLACES)
          .validate(form.getEmissionDailyAverageInput(), errors);
    }
  }
}

package uk.co.nstauthority.fieldconsents.fee;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.nstauthority.fieldconsents.validation.FieldValidationErrorCodes;

@Component
public class FeePeriodFormValidator {

  static final String AMOUNT_REQUIRED_ERROR_MESSAGE = "Enter a cost for %s";
  static final String AMOUNT_INVALID_ERROR_MESSAGE = "Enter a valid cost for %s";
  static final String AMOUNT_NEGATIVE_ERROR_MESSAGE = "%s cost must be 0 or more";

  private final FeePeriodService feePeriodService;

  @Autowired
  FeePeriodFormValidator(FeePeriodService feePeriodService) {
    this.feePeriodService = feePeriodService;
  }

  public void validate(
      FeePeriodForm form,
      @Nullable FeePeriodDto editingFeePeriodDto,
      List<FeeLineDto> feeLineDtos,
      Errors errors
  ) {
    var startDateInput = form.getStartDateInput();

    ThreeFieldDateInputValidator.builder().validate(startDateInput, errors);

    if (!startDateInput.fieldHasErrors(errors)) {
      var startDate = startDateInput.getAsLocalDate()
          .orElseThrow(() -> new IllegalStateException("Error parsing start date"));
      var startDateValidationResult = editingFeePeriodDto == null
          ? feePeriodService.validateCreateFeePeriodStartDate(startDate)
          : feePeriodService.validateEditFeePeriodStartDate(editingFeePeriodDto, startDate);

      if (!startDateValidationResult.valid()) {
        var dayInputFieldName = startDateInput.getDayInput().getFieldName();
        errors.rejectValue(
            dayInputFieldName + ".inputValue",
            FieldValidationErrorCodes.INVALID.errorCode(dayInputFieldName),
            startDateValidationResult.errorMessage()
        );

        var monthInputFieldName = startDateInput.getMonthInput().getFieldName();
        errors.rejectValue(
            monthInputFieldName + ".inputValue",
            FieldValidationErrorCodes.INVALID.errorCode(monthInputFieldName),
            ""
        );

        var yearInputFieldName = startDateInput.getYearInput().getFieldName();
        errors.rejectValue(
            yearInputFieldName + ".inputValue",
            FieldValidationErrorCodes.INVALID.errorCode(yearInputFieldName),
            ""
        );
      }
    }

    var feeLineAmountsByMnemonic = form.getFeeLineAmountsByMnemonic();

    feeLineDtos.forEach(feeLineDto -> {
      var mnemonic = feeLineDto.mnemonic();
      var fieldName = "feeLineAmountsByMnemonic[%s]".formatted(mnemonic);

      ValidationUtils.rejectIfEmptyOrWhitespace(
          errors,
          fieldName,
          FieldValidationErrorCodes.REQUIRED.errorCode(fieldName),
          AMOUNT_REQUIRED_ERROR_MESSAGE.formatted(feeLineDto.title())
      );

      if (errors.hasFieldErrors(fieldName)) {
        return;
      }

      try {
        double amount = Double.parseDouble(feeLineAmountsByMnemonic.get(mnemonic));

        if (amount < 0) {
          errors.rejectValue(
              fieldName,
              FieldValidationErrorCodes.INVALID.errorCode(fieldName),
              AMOUNT_NEGATIVE_ERROR_MESSAGE.formatted(feeLineDto.title())
          );
        }
      } catch (NumberFormatException exception) {
        errors.rejectValue(
            fieldName,
            FieldValidationErrorCodes.INVALID.errorCode(fieldName),
            AMOUNT_INVALID_ERROR_MESSAGE.formatted(feeLineDto.title())
        );
      }
    });
  }
}

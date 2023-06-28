package uk.co.nstauthority.fieldconsents.validation;

import static uk.co.nstauthority.fieldconsents.validation.FieldValidationErrorCodes.REQUIRED;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class ValidatorUtils {

  public static final int MAX_DECIMAL_PLACES = 6;

  private ValidatorUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * invoke validator on a nested object while safely pushing and popping the nested object path.
   */
  public static void invokeNestedValidator(Errors errors,
                                           Validator validator,
                                           String targetPath,
                                           Object targetObject,
                                           Object... validationHints) {
    try {
      errors.pushNestedPath(targetPath);
      ValidationUtils.invokeValidator(validator, targetObject, errors, validationHints);
    } finally {
      errors.popNestedPath();
    }
  }

  public static void validateDateTimeIsInFutureByHours(String dateFieldName,
                                                       String hoursFieldName,
                                                       String minutesFieldName,
                                                       String displayPrefix,
                                                       int mustBeHoursAhead,
                                                       String dateStr,
                                                       String hoursStr,
                                                       String minutesStr,
                                                       Clock clock,
                                                       Errors errors) {
    displayPrefix = displayPrefix.toLowerCase();

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, dateFieldName,
        REQUIRED.errorCode(dateFieldName), "Pick a " + displayPrefix + " date");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, hoursFieldName,
        REQUIRED.errorCode(hoursFieldName), "Enter the " + displayPrefix + " time");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, minutesFieldName,
        REQUIRED.errorCode(minutesFieldName), "");

    var mustBeValidErrorMessage = StringUtils.capitalize(displayPrefix) +
        " must be a valid date in the format dd/mm/yyyy and time in hours and minutes";

    try {
      if (Objects.nonNull(dateStr)) {
        var date = DateUtils.datePickerStringToDate(dateStr);
        if (date.isBefore(LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()))) {
          errors.rejectValue(dateFieldName, FieldValidationErrorCodes.BEFORE_TODAY.errorCode(dateFieldName),
              StringUtils.capitalize(displayPrefix) + " date must be on or after today");
        }
      }
    } catch (DateTimeParseException e) {
      errors.rejectValue(dateFieldName, FieldValidationErrorCodes.INVALID.errorCode(dateFieldName),
          mustBeValidErrorMessage);
    }

    try {
      if (Objects.nonNull(dateStr) && Objects.nonNull(hoursStr) && Objects.nonNull(minutesStr)) {
        var dateTime = DateUtils.datePickerWithTimeStringToDateTime(dateStr, hoursStr, minutesStr);
        if (dateTime
            .isBefore(LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault()).plusHours(mustBeHoursAhead))) {
          errors.rejectValue(hoursFieldName, FieldValidationErrorCodes.BEFORE_SOME_DATE_TIME.errorCode(hoursFieldName),
              StringUtils.capitalize(displayPrefix) + " must be at least " + mustBeHoursAhead +
                  " hour" + addConditionalPlural(mustBeHoursAhead) + " ahead of now");
          errors.rejectValue(minutesFieldName, FieldValidationErrorCodes.BEFORE_SOME_DATE_TIME.errorCode(minutesFieldName),
              "");
        }
      }
    } catch (DateTimeParseException e) {
      errors.rejectValue(hoursFieldName, FieldValidationErrorCodes.INVALID.errorCode(hoursFieldName),
          mustBeValidErrorMessage);
      errors.rejectValue(minutesFieldName, FieldValidationErrorCodes.INVALID.errorCode(minutesFieldName), "");
    }
  }

  private static String addConditionalPlural(int count) {
    return count > 1 ? "s" : "";
  }
}

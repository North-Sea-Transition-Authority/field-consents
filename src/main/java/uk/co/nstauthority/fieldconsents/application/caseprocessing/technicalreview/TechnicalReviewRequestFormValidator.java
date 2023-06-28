package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.validation.FieldValidationErrorCodes.REQUIRED;

import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
public class TechnicalReviewRequestFormValidator implements Validator {

  static final String TECHNICAL_REVIEWER_FIELD_NAME = "technicalReviewerWuaId";
  static final String TECHNICAL_REVIEWER_EMPTY = "Select a technical reviewer";
  static final String DEADLINE_DATE_FIELD_NAME = "deadlineDate";
  static final String DEADLINE_HOURS_FIELD_NAME = "deadlineHours";
  static final String DEADLINE_MINUTES_FIELD_NAME = "deadlineMinutes";
  static final int MUST_BE_HOURS_AHEAD = 1;

  private final Clock clock;

  @Autowired
  public TechnicalReviewRequestFormValidator(Clock clock) {
    this.clock = clock;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return TechnicalReviewRequestForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (TechnicalReviewRequestForm) target;

    ValidationUtils.rejectIfEmpty(errors, TECHNICAL_REVIEWER_FIELD_NAME,
        REQUIRED.errorCode(TECHNICAL_REVIEWER_FIELD_NAME), TECHNICAL_REVIEWER_EMPTY);

    ValidatorUtils.validateDateTimeIsInFutureByHours(
        DEADLINE_DATE_FIELD_NAME,
        DEADLINE_HOURS_FIELD_NAME,
        DEADLINE_MINUTES_FIELD_NAME,
        "deadline",
        MUST_BE_HOURS_AHEAD,
        form.getDeadlineDate(),
        form.getDeadlineHours(),
        form.getDeadlineMinutes(),
        clock,
        errors
    );
  }
}

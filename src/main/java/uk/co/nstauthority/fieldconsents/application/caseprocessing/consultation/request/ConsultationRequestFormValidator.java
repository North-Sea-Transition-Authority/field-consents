package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.request;

import java.time.Clock;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Component
class ConsultationRequestFormValidator implements Validator {

  private final Clock clock;

  ConsultationRequestFormValidator(Clock clock) {
    this.clock = clock;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ConsultationRequestForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (ConsultationRequestForm) target;

    ValidatorUtils.validateDateTimeIsInFutureByHours(
        "deadlineDate",
        "deadlineHours",
        "deadlineMinutes",
        "deadline",
        1,
        form.deadlineDate(),
        form.deadlineHours(),
        form.deadlineMinutes(),
        clock,
        errors
    );
  }
}

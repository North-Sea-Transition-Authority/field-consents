package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class WithdrawalResponseFormValidator implements Validator {

  static final String WITHDRAWAL_RESPONSE_EMPTY = "Select your response to the withdrawal request";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return WithdrawalResponseForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (WithdrawalResponseForm) target;

    ValidationUtils.rejectIfEmpty(errors, "responseStatus", "responseStatus.required", WITHDRAWAL_RESPONSE_EMPTY);

    if (WithdrawalStatus.REJECTED.equals(form.getResponseStatus())) {
      StringInputValidator.builder()
          .validate(form.getResponseText(), errors);
    }
  }
}

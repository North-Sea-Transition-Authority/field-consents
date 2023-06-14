package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class WithdrawalRequestFormValidator implements Validator {

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return WithdrawalRequestForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (WithdrawalRequestForm) target;

    StringInputValidator.builder()
        .validate(form.getRequestText(), errors);
  }
}

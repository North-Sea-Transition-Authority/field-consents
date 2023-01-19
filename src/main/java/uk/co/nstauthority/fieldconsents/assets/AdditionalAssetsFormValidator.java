package uk.co.nstauthority.fieldconsents.assets;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service
public class AdditionalAssetsFormValidator implements Validator {

  public static final String HAS_OTHER_ASSETS_TO_ADD_EMPTY = "Select whether you have more fields to add";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return AdditionalAssetsForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {

    ValidationUtils.rejectIfEmpty(errors, "hasOtherAssetsToAdd", "hasOtherAssetsToAdd.required",
        HAS_OTHER_ASSETS_TO_ADD_EMPTY);

  }
}

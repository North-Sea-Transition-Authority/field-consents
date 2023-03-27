package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class VentFormValidator implements Validator {

  public static final int DESCRIPTION_MAX_CHAR_COUNT = 300;
  public static final int COMMENTS_MAX_CHARACTER_COUNT = 300;
  public static final String VENT_TYPE_EMPTY = "Select the vent type";
  public static final String METERED_FLAG_EMPTY = "Select whether the vent is metered or not";
  public static final String COMMENTS_TOO_LONG =
      String.format("Comments must be no more than %s characters long, " +
              "if circumstances require more detailed explanation " +
              "please provide this as a supporting document later in the form",
          COMMENTS_MAX_CHARACTER_COUNT);

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return VentForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    VentForm ventForm = (VentForm) target;

    ValidationUtils.rejectIfEmpty(errors, "ventType", "ventType.required",
        VENT_TYPE_EMPTY);

    StringInputValidator.builder()
        .mustHaveCharacterCountAtMost(DESCRIPTION_MAX_CHAR_COUNT)
        .validate(ventForm.getDescription(), errors);

    ValidationUtils.rejectIfEmpty(errors, "meteredFlag", "meteredFlag.required",
        METERED_FLAG_EMPTY);

    Boolean meteredFlag = ventForm.getMeteredFlag();
    if (meteredFlag != null) {
      if (meteredFlag) {
        StringInputValidator.builder()
            .isOptional()
            .mustHaveCharacterCountAtMost(COMMENTS_MAX_CHARACTER_COUNT)
            .mustHaveCharacterCountAtMostErrorMessage(COMMENTS_TOO_LONG)
            .validate(ventForm.getCommentsMeteredYes(), errors);
      } else {
        StringInputValidator.builder()
            .mustHaveCharacterCountAtMost(COMMENTS_MAX_CHARACTER_COUNT)
            .mustHaveCharacterCountAtMostErrorMessage(COMMENTS_TOO_LONG)
            .validate(ventForm.getCommentsMeteredNo(), errors);
      }
    }
  }
}

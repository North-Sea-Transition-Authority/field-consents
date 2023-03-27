package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class FlareFormValidator implements Validator {

  public static final int DESCRIPTION_MAX_CHAR_COUNT = 300;
  public static final int COMMENTS_MAX_CHARACTER_COUNT = 300;
  public static final String FLARE_TYPE_EMPTY = "Select the flare type";
  public static final String METERED_FLAG_EMPTY = "Select whether the flare is metered or not";
  public static final String COMMENTS_TOO_LONG =
      String.format("Comments must be no more than %s characters long, " +
              "if circumstances require more detailed explanation " +
              "please provide this as a supporting document later in the form",
          COMMENTS_MAX_CHARACTER_COUNT);

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareForm flareForm = (FlareForm) target;

    ValidationUtils.rejectIfEmpty(errors, "flareType", "flareType.required",
        FLARE_TYPE_EMPTY);

    StringInputValidator.builder()
        .mustHaveCharacterCountAtMost(DESCRIPTION_MAX_CHAR_COUNT)
        .validate(flareForm.getDescription(), errors);

    ValidationUtils.rejectIfEmpty(errors, "meteredFlag", "meteredFlag.required",
        METERED_FLAG_EMPTY);

    Boolean meteredFlag = flareForm.getMeteredFlag();
    if (meteredFlag != null) {
      if (meteredFlag) {
        StringInputValidator.builder()
            .isOptional()
            .mustHaveCharacterCountAtMost(COMMENTS_MAX_CHARACTER_COUNT)
            .mustHaveCharacterCountAtMostErrorMessage(COMMENTS_TOO_LONG)
            .validate(flareForm.getCommentsMeteredYes(), errors);
      } else {
        StringInputValidator.builder()
            .mustHaveCharacterCountAtMost(COMMENTS_MAX_CHARACTER_COUNT)
            .mustHaveCharacterCountAtMostErrorMessage(COMMENTS_TOO_LONG)
            .validate(flareForm.getCommentsMeteredNo(), errors);
      }
    }
  }
}

package uk.co.nstauthority.fieldconsents.flarevent.flare;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class FlareFormValidator implements Validator {

  public static final int descriptionMaxCharCount = 300;
  public static final int commentsMaxCharacterCount = 300;
  public static final String FLARE_TYPE_EMPTY = "Select the flare type";
  public static final String METERED_FLAG_EMPTY = "Select whether the flare is metered or not";
  public static final String COMMENTS_TOO_LONG =
      String.format("Comments must be no more than %s characters long, " +
              "if circumstances require more detailed explanation " +
              "please provide this as a supporting document later in the form",
          commentsMaxCharacterCount);

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareForm flareForm = (FlareForm) target;

    ValidationUtils.rejectIfEmpty(errors, "flareType", "flareType.required",
        FLARE_TYPE_EMPTY);

    ValidationUtils.rejectIfEmpty(errors, "meteredFlag", "meteredFlag.required",
        METERED_FLAG_EMPTY);

    StringInputValidator.builder()
        .mustHaveCharacterCountAtMost(descriptionMaxCharCount)
        .validate(flareForm.getDescription(), errors);

    Boolean meteredFlag = flareForm.getMeteredFlag();
    if (meteredFlag != null) {
      if (meteredFlag) {
        StringInputValidator.builder()
            .isOptional()
            // TODO add back once the DFL develop-SNAPSHOT is up to date
            // .mustHaveCharacterCountAtMost(commentsMaxCharacterCount, COMMENTS_TOO_LONG)
            .mustHaveCharacterCountAtMost(commentsMaxCharacterCount)
            .validate(flareForm.getCommentsMeteredYes(), errors);
      } else {
        StringInputValidator.builder()
            // TODO add back once the DFL develop-SNAPSHOT is up to date
            // .mustHaveCharacterCountAtMost(commentsMaxCharacterCount, COMMENTS_TOO_LONG)
            .mustHaveCharacterCountAtMost(commentsMaxCharacterCount)
            .validate(flareForm.getCommentsMeteredNo(), errors);
      }
    }
  }
}

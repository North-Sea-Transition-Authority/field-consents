package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentFormValidator.TECHNICAL_REVIEWER_EMPTY;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class TechnicalReviewAssignmentFormValidatorTest {

  private Errors errors;

  private TechnicalReviewAssignmentFormValidator formValidator;

  private TechnicalReviewAssignmentForm form;

  @BeforeEach
  void setUp() {
    formValidator = new TechnicalReviewAssignmentFormValidator();
    form = new TechnicalReviewAssignmentForm();
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(TechnicalReviewAssignmentForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    form.setTechnicalReviewerWuaId(WebUserAccountId.valueOf("1"));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_nullTechnicalReviewer_thenError() {
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("technicalReviewerWuaId", Collections.singletonList(TECHNICAL_REVIEWER_EMPTY))
    );
  }
}

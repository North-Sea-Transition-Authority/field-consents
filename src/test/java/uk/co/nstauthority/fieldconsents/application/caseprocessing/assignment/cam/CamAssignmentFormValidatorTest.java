package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam.CamAssignmentFormValidator.CAM_EMPTY;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class CamAssignmentFormValidatorTest {
  private Errors errors;

  private CamAssignmentFormValidator formValidator;

  private CamAssignmentForm form;

  @BeforeEach
  void setUp() {
    formValidator = new CamAssignmentFormValidator();
    form = new CamAssignmentForm();
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(CamAssignmentForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    form.setCamWuaId(WebUserAccountId.valueOf("1"));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_nullCamUser_thenError() {
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("camWuaId", Collections.singletonList(CAM_EMPTY))
    );
  }
}

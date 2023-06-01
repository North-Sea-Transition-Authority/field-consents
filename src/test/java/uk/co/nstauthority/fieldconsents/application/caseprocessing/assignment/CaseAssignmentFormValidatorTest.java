package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentFormValidator.CASE_OFFICER_EMPTY;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class CaseAssignmentFormValidatorTest {

  private Errors errors;

  private CaseAssignmentFormValidator formValidator;

  private CaseAssignmentForm form;

  @BeforeEach
  void setUp() {
    formValidator = new CaseAssignmentFormValidator();
    form = new CaseAssignmentForm();
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(CaseAssignmentForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    form.setCaseOfficerWuaId(WebUserAccountId.valueOf("1"));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_nullCaseOfficer_thenError() {
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("caseOfficerWuaId", Collections.singletonList(CASE_OFFICER_EMPTY))
    );
  }
}

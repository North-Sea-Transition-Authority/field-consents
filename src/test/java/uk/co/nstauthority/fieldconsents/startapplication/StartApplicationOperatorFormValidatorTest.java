package uk.co.nstauthority.fieldconsents.startapplication;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class StartApplicationOperatorFormValidatorTest {

  private Errors errors;

  private StartApplicationOperatorForm form;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @InjectMocks
  private StartApplicationOperatorFormValidator formValidator;


  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    form = new StartApplicationOperatorForm(ApplicationType.PRODUCTION);
  }

  @Test
  void validate_whenValidForm() {
    when(organisationUnitService.findOrganisationUnitById(any(), any()))
        .thenReturn(Optional.of(new OrganisationUnitJson(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1,
            ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1)));
    form.setOrganisationUnitId(String.valueOf(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_noOperator() {
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("organisationUnitId.inputValue", Collections.singletonList("Select an operator"))
    );
  }

  @Test
  void validate_whenInvalidForm_invalidOperator() {
    form.setOrganisationUnitId("-1");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("organisationUnitId.inputValue", Collections.singletonList("An operator must be 0 or more"))
    );
  }

  @Test
  void validate_whenInvalidForm_operatorDoesNotExist() {
    when(organisationUnitService.findOrganisationUnitById(any(), any()))
        .thenReturn(Optional.empty());
    form.setOrganisationUnitId("0");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("organisationUnitId.inputValue", Collections.singletonList("That operator does not exist"))
    );
  }

}
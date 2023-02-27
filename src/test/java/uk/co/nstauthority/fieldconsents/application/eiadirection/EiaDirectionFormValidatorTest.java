package uk.co.nstauthority.fieldconsents.application.eiadirection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionFormValidator.EIA_DIRECTION_REF_EMPTY;
import static uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionFormValidator.EIA_DIRECTION_REF_INVALID;
import static uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionFormValidator.HAVE_EIA_DIRECTION_TO_SUBMIT_EMPTY;
import static uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionFormValidator.HAVE_SUBMITTED_EIA_DIRECTION_EMPTY;
import static uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionFormValidator.SUBMIT_DATE_NOT_IN_FUTURE;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication1Json;

import java.time.LocalDate;
import java.time.Year;
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
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class EiaDirectionFormValidatorTest {

  private static final String SUBMIT_DATE_INVALID = "Submission date must be a real date";

  private static final String SUBMIT_DATE_INCOMPLETE = "Enter a complete submission date";

  private static final String EXPLANATION_EMPTY = "Enter an explanation";

  @Mock
  private PetsApplicationService petsApplicationService;

  @InjectMocks
  private EiaDirectionFormValidator formValidator;

  private EiaDirectionForm form;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    form = new EiaDirectionForm();
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(EiaDirectionForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(String.class)).isFalse();
  }

  @Test
  void validate_whenEmptyForm() {
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("haveSubmittedEiaDirection", Collections.singletonList(HAVE_SUBMITTED_EIA_DIRECTION_EMPTY))
    );
  }

  @Test
  void validate_whenMissingSatId() {
    form.setHaveSubmittedEiaDirection(Boolean.TRUE);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("satId", Collections.singletonList(EIA_DIRECTION_REF_EMPTY))
    );
  }

  @Test
  void validate_whenSatIdButInvalid() {
    when(petsApplicationService.findPetsApplicationById(eq(SAT_ID_1), any()))
        .thenReturn(Optional.empty());
    form.setHaveSubmittedEiaDirection(Boolean.TRUE);
    form.setSatId(SAT_ID_1);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("satId", Collections.singletonList(EIA_DIRECTION_REF_INVALID))
    );
  }

  @Test
  void validate_whenSatIdAndValid() {
    when(petsApplicationService.findPetsApplicationById(eq(SAT_ID_1), any()))
        .thenReturn(Optional.of(petsApplication1Json));
    form.setHaveSubmittedEiaDirection(Boolean.TRUE);
    form.setSatId(SAT_ID_1);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenSatToSubmitMissing() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("haveEiaDirectionToSubmit", Collections.singletonList(HAVE_EIA_DIRECTION_TO_SUBMIT_EMPTY))
    );
  }

  @Test
  void validate_whenDateToSubmitMissing() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("latestDateToBeSubmitted.dayInput.inputValue", Collections.singletonList(SUBMIT_DATE_INCOMPLETE)),
        entry("latestDateToBeSubmitted.monthInput.inputValue", Collections.singletonList("")),
        entry("latestDateToBeSubmitted.yearInput.inputValue", Collections.singletonList(""))
    );
  }

  @Test
  void validate_whenDateToSubmitMissingDay() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    form.getLatestDateToBeSubmitted().setMonth(LocalDate.now().getMonthValue());
    form.getLatestDateToBeSubmitted().setYear(Year.now().getValue());
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("latestDateToBeSubmitted.dayInput.inputValue", Collections.singletonList(SUBMIT_DATE_INCOMPLETE))
    );
  }

  @Test
  void validate_whenDateToSubmitMissingMonth() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    form.getLatestDateToBeSubmitted().setDay(LocalDate.now().getDayOfMonth());
    form.getLatestDateToBeSubmitted().setYear(Year.now().getValue());
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("latestDateToBeSubmitted.monthInput.inputValue", Collections.singletonList(SUBMIT_DATE_INCOMPLETE))
    );
  }

  @Test
  void validate_whenDateToSubmitMissingYear() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    form.getLatestDateToBeSubmitted().setDay(LocalDate.now().getDayOfMonth());
    form.getLatestDateToBeSubmitted().setMonth(LocalDate.now().getMonthValue());
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("latestDateToBeSubmitted.yearInput.inputValue", Collections.singletonList(SUBMIT_DATE_INCOMPLETE))
    );
  }

  @Test
  void validate_whenDateToSubmitInvalidDate() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    form.getLatestDateToBeSubmitted().setDay(50);
    form.getLatestDateToBeSubmitted().getMonthInput().setInputValue("a");
    form.getLatestDateToBeSubmitted().setYear(-1);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("latestDateToBeSubmitted.dayInput.inputValue", Collections.singletonList(SUBMIT_DATE_INVALID)),
        entry("latestDateToBeSubmitted.monthInput.inputValue", Collections.singletonList("")),
        entry("latestDateToBeSubmitted.yearInput.inputValue", Collections.singletonList(""))
    );
  }

  @Test
  void validate_whenDateToSubmitNotInFuture() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    form.getLatestDateToBeSubmitted().setDate(LocalDate.now());
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("latestDateToBeSubmitted.dayInput.inputValue", Collections.singletonList(SUBMIT_DATE_NOT_IN_FUTURE)),
        entry("latestDateToBeSubmitted.monthInput.inputValue", Collections.singletonList("")),
        entry("latestDateToBeSubmitted.yearInput.inputValue", Collections.singletonList(""))
    );
  }

  @Test
  void validate_whenDateToSubmitValid() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.TRUE);
    form.getLatestDateToBeSubmitted().setDate(LocalDate.now().plusDays(1));
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenNoEiaNoExplanation() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.FALSE);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry("whyNoEiaDirection.inputValue", Collections.singletonList(EXPLANATION_EMPTY))
    );
  }

  @Test
  void validate_whenNoEia_explanationValid() {
    form.setHaveSubmittedEiaDirection(Boolean.FALSE);
    form.setHaveEiaDirectionToSubmit(Boolean.FALSE);
    form.getWhyNoEiaDirection().setInputValue("test");
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenNoEiaExplanationValid_bloatedForm() {
    form = EiaDirectionTestUtil.getEiaDirectionFormWithAllDataSet(SAT_ID_1);
    errors = new BeanPropertyBindingResult(form, "form");

    formValidator.validate(form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
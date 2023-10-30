package uk.co.nstauthority.fieldconsents.fee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.fivium.digitalpaymentslibrary.fee.StartDateValidationResult;
import uk.co.nstauthority.fieldconsents.validation.FieldValidationErrorCodes;

@ExtendWith(MockitoExtension.class)
class FeePeriodFormValidatorTest {

  @Mock
  private FeePeriodService feePeriodService;

  @InjectMocks
  private FeePeriodFormValidator feePeriodFormValidator;

  @ParameterizedTest
  @NullAndEmptySource
  void validate_startDateInputValuesNullOrEmpty(String valueString) {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDateInput = form.getStartDateInput();

    var dayInput = startDateInput.getDayInput();
    var monthInput = startDateInput.getMonthInput();
    var yearInput = startDateInput.getYearInput();

    dayInput.setInputValue(valueString);
    monthInput.setInputValue(valueString);
    yearInput.setInputValue(valueString);

    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), "1180"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    var dayInputFieldName = dayInput.getFieldName();
    var monthInputFieldName = monthInput.getFieldName();
    var yearInputFieldName = yearInput.getFieldName();

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                dayInputFieldName + ".inputValue",
                dayInputFieldName + ".required",
                "Enter a complete start date"
            ),
            tuple(
                monthInputFieldName + ".inputValue",
                monthInputFieldName + ".required",
                ""
            ),
            tuple(
                yearInputFieldName + ".inputValue",
                yearInputFieldName + ".required",
                ""
            )
        );
  }

  @Test
  void validate_startDateInputValuesInvalid() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDateInput = form.getStartDateInput();

    var dayInput = startDateInput.getDayInput();
    var monthInput = startDateInput.getMonthInput();
    var yearInput = startDateInput.getYearInput();

    dayInput.setInputValue("32");
    monthInput.setInputValue("13");
    yearInput.setInputValue("9000");

    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), "1180"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    var dayInputFieldName = dayInput.getFieldName();
    var monthInputFieldName = monthInput.getFieldName();
    var yearInputFieldName = yearInput.getFieldName();

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                dayInputFieldName + ".inputValue",
                dayInputFieldName + ".invalid",
                "Start date must be a real date"
            ),
            tuple(
                monthInputFieldName + ".inputValue",
                monthInputFieldName + ".invalid",
                ""
            ),
            tuple(
                yearInputFieldName + ".inputValue",
                yearInputFieldName + ".invalid",
                ""
            )
        );
  }

  @Test
  void validate_nullEditingFeePeriodDtoAndValidateCreateFeePeriodStartDateReturnsError() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDate = LocalDate.of(2023, 10, 18);
    var startDateInput = form.getStartDateInput();
    startDateInput.setDate(startDate);

    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), "1180"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    var startDateValidationResultErrorMessage = "testStartDateValidationResultErrorMessage";

    when(feePeriodService.validateCreateFeePeriodStartDate(startDate))
        .thenReturn(new StartDateValidationResult(false, startDateValidationResultErrorMessage));

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    var dayInputFieldName = startDateInput.getDayInput().getFieldName();
    var monthInputFieldName = startDateInput.getMonthInput().getFieldName();
    var yearInputFieldName = startDateInput.getYearInput().getFieldName();

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                dayInputFieldName + ".inputValue",
                dayInputFieldName + ".invalid",
                startDateValidationResultErrorMessage
            ),
            tuple(
                monthInputFieldName + ".inputValue",
                monthInputFieldName + ".invalid",
                ""
            ),
            tuple(
                yearInputFieldName + ".inputValue",
                yearInputFieldName + ".invalid",
                ""
            )
        );
  }

  @Test
  void validate_nonNullEditingFeePeriodDtoAndValidateCreateFeePeriodStartDateReturnsError() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDate = LocalDate.of(2023, 10, 18);
    var startDateInput = form.getStartDateInput();
    startDateInput.setDate(startDate);

    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), "1180"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    var editingFeePeriodDto = new FeePeriodDto(null, null, null);

    var startDateValidationResultErrorMessage = "testStartDateValidationResultErrorMessage";

    when(feePeriodService.validateEditFeePeriodStartDate(editingFeePeriodDto, startDate))
        .thenReturn(new StartDateValidationResult(false, startDateValidationResultErrorMessage));

    feePeriodFormValidator.validate(form, editingFeePeriodDto, feeLineDtos, errors);

    var dayInputFieldName = startDateInput.getDayInput().getFieldName();
    var monthInputFieldName = startDateInput.getMonthInput().getFieldName();
    var yearInputFieldName = startDateInput.getYearInput().getFieldName();

    assertThat(errors.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                dayInputFieldName + ".inputValue",
                dayInputFieldName + ".invalid",
                startDateValidationResultErrorMessage
            ),
            tuple(
                monthInputFieldName + ".inputValue",
                monthInputFieldName + ".invalid",
                ""
            ),
            tuple(
                yearInputFieldName + ".inputValue",
                yearInputFieldName + ".invalid",
                ""
            )
        );
  }

  @Test
  void validate_missingFeeLineAmount() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDate = LocalDate.of(2023, 10, 18);
    form.getStartDateInput().setDate(startDate);
    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    when(feePeriodService.validateCreateFeePeriodStartDate(startDate))
        .thenReturn(new StartDateValidationResult(true, null));

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);
    assertThat(errors.getFieldErrors("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"))
        .extracting(
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                FieldValidationErrorCodes.REQUIRED.errorCode("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"),
                FeePeriodFormValidator.AMOUNT_REQUIRED_ERROR_MESSAGE.formatted(feeLineDto2.title())
            )
        );
  }

  @Test
  void validate_emptyFeeLineAmount() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDate = LocalDate.of(2023, 10, 18);
    form.getStartDateInput().setDate(startDate);
    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), ""
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    when(feePeriodService.validateCreateFeePeriodStartDate(startDate))
        .thenReturn(new StartDateValidationResult(true, null));

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);
    assertThat(errors.getFieldErrors("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"))
        .extracting(
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                FieldValidationErrorCodes.REQUIRED.errorCode("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"),
                FeePeriodFormValidator.AMOUNT_REQUIRED_ERROR_MESSAGE.formatted(feeLineDto2.title())
            )
        );
  }

  @Test
  void validate_invalidFeeLineAmount() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDate = LocalDate.of(2023, 10, 18);
    form.getStartDateInput().setDate(startDate);
    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), "test1"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    when(feePeriodService.validateCreateFeePeriodStartDate(startDate))
        .thenReturn(new StartDateValidationResult(true, null));

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);
    assertThat(errors.getFieldErrors("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"))
        .extracting(
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                FieldValidationErrorCodes.INVALID.errorCode("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"),
                FeePeriodFormValidator.AMOUNT_INVALID_ERROR_MESSAGE.formatted(feeLineDto2.title())
            )
        );
  }

  @Test
  void validate_negativeFeeLineAmount() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDate = LocalDate.of(2023, 10, 18);
    form.getStartDateInput().setDate(startDate);
    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), "-1"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    when(feePeriodService.validateCreateFeePeriodStartDate(startDate))
        .thenReturn(new StartDateValidationResult(true, null));

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);
    assertThat(errors.getFieldErrors("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"))
        .extracting(
            FieldError::getCode,
            FieldError::getDefaultMessage
        )
        .containsExactly(
            tuple(
                FieldValidationErrorCodes.INVALID.errorCode("feeLineAmountsByMnemonic[testFeeLine2Mnemonic]"),
                FeePeriodFormValidator.AMOUNT_NEGATIVE_ERROR_MESSAGE.formatted(feeLineDto2.title())
            )
        );
  }

  @Test
  void validate_valid() {
    var form = new FeePeriodForm();

    var feeLineDto1 = new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100);
    var feeLineDto2 = new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200);

    var startDate = LocalDate.of(2023, 10, 18);
    form.getStartDateInput().setDate(startDate);
    form.setFeeLineAmountsByMnemonic(Map.of(
        feeLineDto1.mnemonic(), "930",
        feeLineDto2.mnemonic(), "1180"
    ));

    var errors = new BeanPropertyBindingResult(form, "form");

    var feeLineDtos = List.of(feeLineDto1, feeLineDto2);

    when(feePeriodService.validateCreateFeePeriodStartDate(startDate))
        .thenReturn(new StartDateValidationResult(true, null));

    feePeriodFormValidator.validate(form, null, feeLineDtos, errors);

    assertThat(errors.getErrorCount()).isZero();
  }
}

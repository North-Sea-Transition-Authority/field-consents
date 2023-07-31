package uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationJson;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;

@ExtendWith(MockitoExtension.class)
class HaveSubmittedFormValidatorTest {

  private static final int SAT_ID = 123;
  private static final String CACHED_SAT_REF = "ref-123";
  private static final String SAT_LOOKUP_PURPOSE = "EIA direction form validation";

  @Mock
  private PetsApplicationService petsApplicationService;

  @InjectMocks
  private HaveSubmittedFormValidator validator;

  @Test
  void supports() {
    assertThat(validator.supports(HaveSubmittedForm.class)).isTrue();
  }

  @Test
  void validate_emptyRadioOnSubmission() {
    var form = new HaveSubmittedForm(null, null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(
            FieldError::getField,
            FieldError::getDefaultMessage
        ).containsExactly(
            "haveSubmittedEiaDirection",
            "Select yes if you have submitted an EIA screening direction"
        );
  }

  @Test
  void validate_noSatId() {
    var form = new HaveSubmittedForm(true, null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(
            FieldError::getField,
            FieldError::getDefaultMessage
        ).containsExactly(
            "satId",
            "Select an EIA screening direction reference"
        );
  }

  @Test
  void validate_noErrors() {
    var form = new HaveSubmittedForm(true, SAT_ID);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var petsApplication = PetsApplicationJson.fromCachedInformation(SAT_ID, CACHED_SAT_REF);
    when(petsApplicationService.findPetsApplicationById(SAT_ID, SAT_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(petsApplication));

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_noSatRefFound() {
    var form = new HaveSubmittedForm(true, SAT_ID);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(petsApplicationService.findPetsApplicationById(SAT_ID, SAT_LOOKUP_PURPOSE)).thenReturn(Optional.empty());

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(
            FieldError::getField,
            FieldError::getDefaultMessage
        ).containsExactly(
            "satId",
            "That EIA screening direction does not exist"
        );
  }

}

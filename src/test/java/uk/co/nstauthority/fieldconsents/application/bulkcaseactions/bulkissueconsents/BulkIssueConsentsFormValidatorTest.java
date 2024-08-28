package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class BulkIssueConsentsFormValidatorTest {

  private static final String SELECTED_APPLICATION_IDS = "selectedApplicationIds";
  private static final String SELECT_AT_LEAST_ONE_APPLICATION = "Select at least one application";

  @InjectMocks
  private BulkIssueConsentsFormValidator bulkIssueConsentsFormValidator;

  @Test
  void validate() {
    var form = new BulkIssueConsentsForm(Set.of("1", "22", "333"));
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    bulkIssueConsentsFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_noSelectedApplicationIds() {
    var form = new BulkIssueConsentsForm(null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    bulkIssueConsentsFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldError(SELECTED_APPLICATION_IDS))
        .isNotNull()
        .extracting(
            FieldError::getField,
            DefaultMessageSourceResolvable::getDefaultMessage
        ).containsExactly(
            SELECTED_APPLICATION_IDS,
            SELECT_AT_LEAST_ONE_APPLICATION
        );
  }

  @Test
  void validate_invalidApplicationIds() {
    var form = new BulkIssueConsentsForm(Set.of(UUID.randomUUID().toString()));
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    bulkIssueConsentsFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldError(SELECTED_APPLICATION_IDS))
        .isNotNull()
        .extracting(
            FieldError::getField,
            DefaultMessageSourceResolvable::getDefaultMessage
        ).containsExactly(
            SELECTED_APPLICATION_IDS,
            SELECT_AT_LEAST_ONE_APPLICATION
        );
  }

}
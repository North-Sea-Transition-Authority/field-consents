package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

@ExtendWith(MockitoExtension.class)
class BulkCaseActionSelectionFormValidatorTest {

  @InjectMocks
  private BulkCaseActionSelectionFormValidator bulkCaseActionSelectionFormValidator;

  @Test
  void validate() {
    var form = new BulkCaseActionSelectionForm(BulkCaseAction.BULK_ISSUE_CONSENTS.name());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    bulkCaseActionSelectionFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_invalid() {
    var form = new BulkCaseActionSelectionForm(UUID.randomUUID().toString());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    bulkCaseActionSelectionFormValidator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isTrue();
  }
}
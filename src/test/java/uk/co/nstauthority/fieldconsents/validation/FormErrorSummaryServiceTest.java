package uk.co.nstauthority.fieldconsents.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionForm;

class FormErrorSummaryServiceTest {

  private FormErrorSummaryService formErrorSummaryService;

  @BeforeEach
  void setUp() {
    formErrorSummaryService = new FormErrorSummaryService();
  }

  @Test
  void getErrorItems_withEmptyBindingResults() {
    List<ErrorItem> errorItemList = formErrorSummaryService.getErrorItems(ReverseRouter.emptyBindingResult());
    assertThat(errorItemList).isEmpty();
  }

  @Test
  void getErrorItems_withBindingResults() {
    var bindingResult = new BeanPropertyBindingResult(new AnnualProductionForm(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));

    List<ErrorItem> errorItemList = formErrorSummaryService.getErrorItems(bindingResult);
    assertThat(errorItemList).hasSize(1);
    assertThat(errorItemList.get(0).getFieldName()).isEqualTo("ErrorField");
    assertThat(errorItemList.get(0).getErrorMessage()).isEqualTo("Error message");
  }
}
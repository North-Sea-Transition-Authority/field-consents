package uk.co.nstauthority.fieldconsents.controllerhelper;

import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.validation.ValidationErrorOrderingService;

@Service
public class ControllerHelperService {

  private final ValidationErrorOrderingService validationErrorOrderingService;

  public ControllerHelperService(ValidationErrorOrderingService validationErrorOrderingService) {
    this.validationErrorOrderingService = validationErrorOrderingService;
  }

  /**
   * Standardises basic form POST behaviour, allows controllers to either return a ModelAndView that's failed validation
   * (populated with validation errors) or do a caller-specified action if passed validation.
   * @param bindingResult result of binding the form object from request
   * @param form the form used to determine the error ordering
   * @param ifInvalid the action to perform if the validation fails
   * @param ifValid the action to perform if the validation passes
   * @return passed-in ModelAndView with validation errors added if validation failed, caller-specified ModelAndView otherwise
   */
  public ModelAndView checkErrorsAndRedirect(BindingResult bindingResult,
                                             Object form,
                                             Supplier<ModelAndView> ifInvalid,
                                             Supplier<ModelAndView> ifValid) {
    if (bindingResult.hasErrors()) {
      var modelAndView = ifInvalid.get();
      addFieldValidationErrors(modelAndView, bindingResult, form);
      return modelAndView;
    }
    return ifValid.get();
  }

  /**
   * Adds field validation errors to a model and view.
   * @param modelAndView The model and view which failed validation
   * @param bindingResult The result of the submitted form containing the list of validation errors
   */
  private void addFieldValidationErrors(ModelAndView modelAndView, BindingResult bindingResult, Object form) {
    final var errorList = validationErrorOrderingService.getErrorItemsFromBindingResult(form, bindingResult);
    modelAndView.addObject("errorList", errorList);
  }
}
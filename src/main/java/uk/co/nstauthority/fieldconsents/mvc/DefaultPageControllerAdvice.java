package uk.co.nstauthority.fieldconsents.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
class DefaultPageControllerAdvice {

  private final ControllerAdviceService controllerAdviceService;

  DefaultPageControllerAdvice(ControllerAdviceService controllerAdviceService) {
    this.controllerAdviceService = controllerAdviceService;
  }

  @ModelAttribute
  void addDefaultModelAttributes(Model model, HttpServletRequest request) {
    controllerAdviceService.addDefaultModelAttributes(model, request);
  }

  @InitBinder
  void initBinder(WebDataBinder binder) {
    // Trim whitespace from form fields
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }
}

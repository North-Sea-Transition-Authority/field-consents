package uk.co.nstauthority.fieldconsents.assets.fields;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class FieldController {

  private final FieldService fieldService;

  @Autowired
  public FieldController(FieldService fieldService) {
    this.fieldService = fieldService;
  }

  @GetMapping("/fields/{fieldId}")
  public ModelAndView manageField(@PathVariable Integer fieldId) {
    return new ModelAndView("fcs/assets/fields")
        .addObject("fieldId", fieldId)
        .addObject("fieldName", fieldService.getFieldOrError(fieldId).fieldName());
  }

}

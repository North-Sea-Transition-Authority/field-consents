package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;

@RestController
@RequestMapping("/fields/{fieldId}")
public class FieldController {

  private final FieldService fieldService;

  @Autowired
  public FieldController(FieldService fieldService) {
    this.fieldService = fieldService;
  }

  @GetMapping
  public ModelAndView manageField(@PathVariable Integer fieldId) {
    return new ModelAndView("fcs/assets/fields")
        .addObject("fieldId", fieldId)
        .addObject("fieldName",
            fieldService.getFieldOrError(fieldId, "Manage field").fieldName())
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromFieldController.class).getStartApplicationForm(fieldId))
        );
  }
}

package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromFieldController;

@RestController
@RequestMapping("/fields/{fieldId}")
public class FieldController {

  public static final String LICENCE_WARNING = """
      You cannot start an application for this field as it does not have any associated licences, \
      please contact %s if you think this field should have associated licences.""";

  private final FieldService fieldService;

  private final CustomerConfigurationProperties customerConfigurationProperties;

  @Autowired
  public FieldController(FieldService fieldService,
                         CustomerConfigurationProperties customerConfigurationProperties) {
    this.fieldService = fieldService;
    this.customerConfigurationProperties = customerConfigurationProperties;
  }

  @GetMapping
  public ModelAndView manageField(@PathVariable Integer fieldId) {

    FieldWithOperatorAndLicencesJson fieldJson
        = fieldService.getFieldWithOperatorAndLicences(fieldId,
        "Check associated licences exist when starting a field application");

    return new ModelAndView("fcs/assets/fields")
        .addObject("fieldId", fieldId)
        .addObject("fieldName", fieldJson.getName())
        .addObject("licencesExist", !fieldJson.getLicences().isEmpty())
        .addObject("warningHeading", "Associated licences missing")
        .addObject("warningContent", LICENCE_WARNING.formatted(customerConfigurationProperties.mnemonic()))
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromFieldController.class).getStartApplicationForm(fieldId))
        );
  }
}

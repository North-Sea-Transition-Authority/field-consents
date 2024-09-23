package uk.co.nstauthority.fieldconsents.fds.navigation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;

@Controller
@RequestMapping("/energy-portal")
@AccessibleByServiceUsers
public class EnergyPortalRedirectController {

  private final EnergyPortalConfiguration energyPortalConfiguration;

  EnergyPortalRedirectController(EnergyPortalConfiguration energyPortalConfiguration) {
    this.energyPortalConfiguration = energyPortalConfiguration;
  }

  @GetMapping()
  public ModelAndView redirectToEnergyPortal() {
    return new ModelAndView("redirect:" + energyPortalConfiguration.workbasketUrl());
  }

}

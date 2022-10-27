package uk.co.nstauthority.fieldconsents.application;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
public class ApplicationRedirectService {

  // TODO: Update this method with corresponding redirect calls when Tasklist controllers have been implemented
  //       FCS-210, FCS-211, FCS-212
  public ModelAndView getTaskListModelAndViewByApplicationType(ApplicationType applicationType) {
    switch (applicationType) {
      case FLARE -> {
        return new ModelAndView("fcs/startapplication/flareApplicationTaskList");
      }
      case VENT -> {
        return new ModelAndView("fcs/startapplication/ventApplicationTaskList");
      }
      case PRODUCTION -> {
        return new ModelAndView("fcs/startapplication/productionApplicationTaskList");
      }
      default -> throw new RuntimeException("Incorrect application type: " + applicationType);
    }
  }
}

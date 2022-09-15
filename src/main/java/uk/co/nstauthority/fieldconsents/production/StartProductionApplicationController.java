package uk.co.nstauthority.fieldconsents.production;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/start-production-application")
public class StartProductionApplicationController {

  public static final String FLARE_VENT_LINK_URL = "https://www.nstauthority.co.uk/licensing-consents/consents/flaring-and-venting/";
  public static final String EXPLORATION_LINK_URL = "https://www.nstauthority.co.uk/exploration-production/exploration/exploration-operatorship";
  public static final String ONSHORE_LINK_URL = "https://www.nstauthority.co.uk/exploration-production/onshore/onshore-operatorship";
  private final ApplicationService applicationService;

  @Autowired
  public StartProductionApplicationController(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  @GetMapping
  public ModelAndView getStartProductionApplicationModelAndView() {
    return new ModelAndView("fcs/startapplication/startProductionApplication")
        .addObject("flareVentLinkUrl", FLARE_VENT_LINK_URL)
        .addObject("explorationLinkUrl", EXPLORATION_LINK_URL)
        .addObject("onshoreLinkUrl", ONSHORE_LINK_URL)
        .addObject("createProductionUrl",
            //    TODO: For now this is redirecting the user to the Start Production Application screen.
            //          At this stage we should allow the user to select the application type and the year.
            ReverseRouter.route(on(StartProductionApplicationController.class).startNewProductionApplication()));
  }

  @PostMapping
  public ModelAndView startNewProductionApplication() {
    applicationService.createNewApplication(ApplicationType.PRODUCTION);
    // TODO: Create a new ProductionApplicatioTaskListController when we'll have a task-list to show for this consent type
    return new ModelAndView("fcs/startapplication/productionApplicationTaskList");
  }

}

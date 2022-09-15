package uk.co.nstauthority.fieldconsents.startapplication;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.StartProductionApplicationController;

@Controller
@RequestMapping("/start-application")
public class StartApplicationController {

  @PostMapping("")
  public ModelAndView startNewApplication() {
    return getNewApplicationModelAndView();
  }

  @NotNull
  private ModelAndView getNewApplicationModelAndView() {
    return ReverseRouter.redirect(on(StartProductionApplicationController.class).startNewProductionApplication());
  }
}

package uk.co.nstauthority.fieldconsents.assets.terminals;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.startapplication.StartApplicationFromTerminalController;

@RestController
@RequestMapping("/facilities/{terminalId}")
public class TerminalController {

  private final TerminalService terminalService;

  @Autowired
  public TerminalController(TerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @GetMapping
  public ModelAndView manageTerminal(@PathVariable Integer terminalId) {
    return new ModelAndView("fcs/assets/terminals")
        .addObject("terminalId", terminalId)
        .addObject("terminalName",
            terminalService.getTerminalOrError(terminalId, "Manage terminal").terminalName())
        .addObject("startApplicationUrl",
            ReverseRouter.route(on(StartApplicationFromTerminalController.class).getStartApplicationForm(terminalId))
        );
  }
}

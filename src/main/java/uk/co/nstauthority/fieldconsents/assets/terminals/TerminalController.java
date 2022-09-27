package uk.co.nstauthority.fieldconsents.assets.terminals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class TerminalController {

  private final TerminalService terminalService;

  @Autowired
  public TerminalController(TerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @GetMapping("/terminal/{terminalId}")
  public ModelAndView manageTerminal(@PathVariable Integer terminalId) {
    return new ModelAndView("fcs/assets/terminals")
        .addObject("terminalId", terminalId)
        .addObject("terminalName", terminalService.getTerminalOrError(terminalId, "Manage terminal").terminalName());
  }

}

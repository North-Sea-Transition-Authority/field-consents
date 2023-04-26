package uk.co.nstauthority.fieldconsents.workarea;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;

@Controller
// the ordering of the mappings is import here otherwise the top navigation always highlights the work area
// even if another page is on display
@RequestMapping({"/work-area", "/"})
@AccessibleByServiceUsers
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";

  @GetMapping
  public ModelAndView getWorkArea() {
    return new ModelAndView("fcs/workarea/workArea")
        .addObject("pageTitle", WORK_AREA_TITLE);
  }
}

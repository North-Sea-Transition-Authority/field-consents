package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;

@Controller
@RequestMapping("applications/{applicationId}/task-list")
public class ApplicationTaskListController {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationTaskListService applicationTaskListService;


  ApplicationTaskListController(ApplicationVersionService applicationVersionService,
                                ApplicationTaskListService applicationTaskListService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationTaskListService = applicationTaskListService;
  }

  @GetMapping
  public ModelAndView getTaskList(@PathVariable Integer applicationId) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var sections = applicationTaskListService.getAllSections(applicationVersion);

    var applicationType = applicationVersion.getApplication().getType().getDisplayName();

    return new ModelAndView("fcs/application/applicationTaskList")
        .addObject("pageTitle", applicationType + " application tasklist")
        .addObject("taskListSections", sections);
  }
}

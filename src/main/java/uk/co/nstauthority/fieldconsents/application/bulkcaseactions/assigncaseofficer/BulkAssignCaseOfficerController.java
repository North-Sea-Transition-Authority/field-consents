package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.servlet.http.HttpSession;
import java.util.function.Function;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSearchController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("bulk-case-actions/assign-case-officer")
@HasPermission(permissions = RolePermission.ASSIGN_FCS_APPLICATIONS)
public class BulkAssignCaseOfficerController {

  public static final String ASSIGN_CASE_OFFICER = "Assign case officer";

  private final BulkCaseActionService bulkCaseActionService;

  BulkAssignCaseOfficerController(BulkCaseActionService bulkCaseActionService) {
    this.bulkCaseActionService = bulkCaseActionService;
  }

  @GetMapping
  public ModelAndView assignCaseOfficer(HttpSession session, ServiceUserDetail user) {
    return new ModelAndView("fcs/application/bulk-case-actions/assignCaseOfficer")
        .addObject("pageTitle", ASSIGN_CASE_OFFICER)
        .addObject("backLinkUrl", ReverseRouter.route(on(BulkCaseActionSearchController.class).getSearchResults(null, null)))
        .addObject("applicationDataItems", bulkCaseActionService.getSelectedApplicationDataItems(session, user))
        .addObject("captionHeadingFunction", (Function<ApplicationDataItem, String>) this::captionHeadingFunction);
  }

  private String captionHeadingFunction(ApplicationDataItem applicationDataItem) {
    if (applicationDataItem.caseOfficer().isEmpty()) {
      return "No case officer currently assigned";
    }

    return "Current case officer: %s".formatted(applicationDataItem.caseOfficer());
  }

}

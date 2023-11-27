package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("bulk-case-actions/search")
@HasPermission(permissions = RolePermission.ASSIGN_FCS_APPLICATIONS)
public class BulkCaseActionSearchController {

  public static final String PAGE_TITLE = "Bulk case actions";

  private final BulkCaseActionService bulkCaseActionService;

  BulkCaseActionSearchController(BulkCaseActionService bulkCaseActionService) {
    this.bulkCaseActionService = bulkCaseActionService;
  }

  @GetMapping
  public ModelAndView getSearchResults(ServiceUserDetail user) {
    return new ModelAndView("fcs/application/bulk-case-actions/search")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("applicationDataItems", bulkCaseActionService.getApplicationDataItems(user));
  }

}

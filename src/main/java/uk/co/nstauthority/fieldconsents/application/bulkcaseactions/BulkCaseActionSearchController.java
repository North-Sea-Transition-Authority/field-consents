package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("bulk-case-actions/search")
@HasPermission(permissions = RolePermission.ASSIGN_FCS_APPLICATIONS)
public class BulkCaseActionSearchController {

  public static final String FORM_SESSION_ATTRIBUTE = "bulkCaseActionSearchForm";
  public static final String PAGE_TITLE = "Bulk case actions";

  private final BulkCaseActionService bulkCaseActionService;

  BulkCaseActionSearchController(BulkCaseActionService bulkCaseActionService) {
    this.bulkCaseActionService = bulkCaseActionService;
  }

  @GetMapping
  public ModelAndView getSearchResults(HttpSession session, ServiceUserDetail user) {
    var applicationDataItems = bulkCaseActionService.getApplicationDataItems(user);
    var form = getSearchForm(session, applicationDataItems);

    return searchResultsModelAndView(applicationDataItems, form);
  }

  private BulkCaseActionSearchForm getSearchForm(HttpSession session, List<ApplicationDataItem> applicationDataItems) {
    var selectedApplicationIds = bulkCaseActionService.getSelectedApplicationIds(session);
    if (selectedApplicationIds.isEmpty()) {
      return BulkCaseActionSearchForm.empty();
    }

    var availableApplicationIds = applicationDataItems.stream()
        .map(ApplicationDataItem::applicationId)
        .filter(selectedApplicationIds::contains)
        .map(String::valueOf)
        .toList();

    return new BulkCaseActionSearchForm(availableApplicationIds);
  }

  private ModelAndView searchResultsModelAndView(List<ApplicationDataItem> applicationDataItems, BulkCaseActionSearchForm form) {
    return new ModelAndView("fcs/application/bulk-case-actions/search")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("actions", getAvailableActions())
        .addObject("applicationDataItems", applicationDataItems)
        .addObject("form", form);
  }

  private List<String> getAvailableActions() {
    return List.of(ASSIGN_CASE_OFFICER);
  }

  @PostMapping(params = ASSIGN_CASE_OFFICER)
  ModelAndView submitAssignCaseOfficerSelection(
      @Valid @ModelAttribute("form") BulkCaseActionSearchForm form,
      BindingResult bindingResult,
      HttpSession session,
      ServiceUserDetail user
  ) {
    if (bindingResult.hasErrors()) {
      var applicationDataItems = bulkCaseActionService.getApplicationDataItems(user);
      return searchResultsModelAndView(applicationDataItems, form);
    }

    session.setAttribute(FORM_SESSION_ATTRIBUTE, form);

    return ReverseRouter.redirect(on(BulkAssignCaseOfficerController.class).assignCaseOfficer(null, null));
  }

}

package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;

@Service
public class BulkCaseActionControllerHelperService {

  public static final String SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE = "bulkCaseActionSelectedApplicationsForm";
  public static final String FILTERS_FORM_SESSION_ATTRIBUTE = "bulkCaseActionSearchFilterForm";

  public BulkCaseActionSelectedApplicationsForm getSelectedApplicationsForm(
      HttpSession session,
      List<ApplicationDataItem> applicationDataItems
  ) {
    var selectedApplicationIds = getSelectedApplicationsForm(session).selectedApplicationIds()
        .stream()
        .map(Integer::parseInt)
        .collect(Collectors.toSet());

    if (selectedApplicationIds.isEmpty()) {
      return BulkCaseActionSelectedApplicationsForm.empty();
    }

    var availableApplicationIds = applicationDataItems.stream()
        .map(ApplicationDataItem::applicationId)
        .filter(selectedApplicationIds::contains)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    return new BulkCaseActionSelectedApplicationsForm(availableApplicationIds);
  }

  public BulkCaseActionSelectedApplicationsForm getSelectedApplicationsForm(HttpSession session) {
    return Optional.ofNullable(session.getAttribute(SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE))
        .filter(BulkCaseActionSelectedApplicationsForm.class::isInstance)
        .map(BulkCaseActionSelectedApplicationsForm.class::cast)
        .orElseGet(BulkCaseActionSelectedApplicationsForm::empty);
  }

  BulkCaseActionSearchFiltersForm getSearchFiltersForm(HttpSession session) {
    return Optional.ofNullable(session.getAttribute(FILTERS_FORM_SESSION_ATTRIBUTE))
        .filter(BulkCaseActionSearchFiltersForm.class::isInstance)
        .map(BulkCaseActionSearchFiltersForm.class::cast)
        .orElseGet(BulkCaseActionSearchFiltersForm::empty);
  }

  void updateSearchFilters(HttpSession session, BulkCaseActionSearchFiltersForm form) {
    session.setAttribute(FILTERS_FORM_SESSION_ATTRIBUTE, form);
  }

  void clearSearchFilters(HttpSession session) {
    session.removeAttribute(FILTERS_FORM_SESSION_ATTRIBUTE);
  }

  void updateSelectedApplicationsForm(HttpSession session, BulkCaseActionSelectedApplicationsForm form) {
    session.setAttribute(SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE, form);
  }

}

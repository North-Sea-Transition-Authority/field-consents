package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;

public class BulkCaseActionSelectedApplicationsForm implements Serializable {

  @Serial
  private static final long serialVersionUID = -3249381657497352072L;

  @NotEmpty(message = "Select at least one application")
  private final Set<Integer> selectedApplicationIds;

  public BulkCaseActionSelectedApplicationsForm() {
    this.selectedApplicationIds = new HashSet<>();
  }

  public Set<Integer> getSelectedApplicationIds() {
    return selectedApplicationIds;
  }

  public void setSelectedApplicationIds(Collection<Integer> selectedApplicationIds) {
    this.selectedApplicationIds.clear();
    this.selectedApplicationIds.addAll(selectedApplicationIds);
  }

  public void removeUnavailableApplications(Collection<ApplicationDataItemView> applicationDataItemViews) {
    if (selectedApplicationIds.isEmpty()) {
      return;
    }

    var availableApplicationIds = applicationDataItemViews.stream()
        .map(ApplicationDataItemView::applicationId)
        .collect(Collectors.toSet());

    selectedApplicationIds.removeIf(applicationId -> !availableApplicationIds.contains(applicationId));
  }

}

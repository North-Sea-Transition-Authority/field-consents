package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

public record BulkCaseActionSelectedApplicationsForm(
    @NotNull(message = "Select at least one application") Set<String> selectedApplicationIds
) {

  public static BulkCaseActionSelectedApplicationsForm empty() {
    return new BulkCaseActionSelectedApplicationsForm(Collections.emptySet());
  }

}

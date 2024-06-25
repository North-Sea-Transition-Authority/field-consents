package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public record BulkCaseActionSelectedApplicationsForm(
    @NotNull(message = "Select at least one application") Set<String> selectedApplicationIds
) implements Serializable {

  @Serial
  private static final long serialVersionUID = -3249381657497352072L;

  public static BulkCaseActionSelectedApplicationsForm empty() {
    return new BulkCaseActionSelectedApplicationsForm(Collections.emptySet());
  }

}

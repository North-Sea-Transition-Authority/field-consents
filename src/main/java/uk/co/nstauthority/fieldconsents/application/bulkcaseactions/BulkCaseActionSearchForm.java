package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public record BulkCaseActionSearchForm(
    @NotNull(message = "Select at least one application") List<String> selectedApplicationIds
) {

  public static BulkCaseActionSearchForm empty() {
    return new BulkCaseActionSearchForm(Collections.emptyList());
  }

}

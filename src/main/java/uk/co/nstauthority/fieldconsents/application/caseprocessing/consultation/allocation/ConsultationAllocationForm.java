package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.allocation;

import jakarta.validation.constraints.NotNull;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;

public record ConsultationAllocationForm(
    @NotNull(message = "Select a responder")
    WebUserAccountId allocatedResponder
) {

  public static ConsultationAllocationForm empty() {
    return new ConsultationAllocationForm(null);
  }

}

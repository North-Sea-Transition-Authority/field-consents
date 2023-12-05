package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import java.util.Collections;
import java.util.Set;

public record BulkAssignCaseOfficerForm(
    String caseOfficerWuaId,
    Set<String> selectedApplicationIds
) {

  public static BulkAssignCaseOfficerForm empty() {
    return new BulkAssignCaseOfficerForm(null, Collections.emptySet());
  }

}

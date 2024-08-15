package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import java.util.Collections;
import java.util.Set;

record BulkIssueConsentsForm(
    Set<String> selectedApplicationIds
) {

  static BulkIssueConsentsForm empty() {
    return new BulkIssueConsentsForm(Collections.emptySet());
  }

}

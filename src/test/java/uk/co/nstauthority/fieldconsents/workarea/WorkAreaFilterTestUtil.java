package uk.co.nstauthority.fieldconsents.workarea;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

public class WorkAreaFilterTestUtil {

  static WorkAreaFilter getDefaultFilter() {
    var workAreaFilter = new WorkAreaFilter();
    workAreaFilter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    workAreaFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return workAreaFilter;
  }

  static WorkAreaFilter getFilterWithDurationTypes() {
    var workAreaFilter = new WorkAreaFilter();
    workAreaFilter.setDurationTypes(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM));
    return workAreaFilter;
  }

  static WorkAreaForm getWorkAreaFormForDefaultFilter() {
    var workAreaForm = new WorkAreaForm();
    workAreaForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    workAreaForm.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return workAreaForm;
  }

  static WorkAreaForm getWorkAreaFormForFilterWithDurationTypes() {
    var workAreaForm = new WorkAreaForm();
    workAreaForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM));
    return workAreaForm;
  }
}

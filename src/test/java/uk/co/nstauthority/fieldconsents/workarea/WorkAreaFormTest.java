package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorkAreaFormTest {

  @Test
  void from_withDefaultWorkAreaFilter() {
    var defaultFilter = WorkAreaFilterTestUtil.getDefaultFilter();
    var workAreaForm = WorkAreaFilterTestUtil.getWorkAreaFormForDefaultFilter();

    assertThat(WorkAreaForm.from(defaultFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }

  @Test
  void from_withConsentDurationsWorkAreaFilter() {
    var durationTypesFilter = WorkAreaFilterTestUtil.getFilterWithDurationTypes();
    var workAreaForm = WorkAreaFilterTestUtil.getWorkAreaFormForFilterWithDurationTypes();

    assertThat(WorkAreaForm.from(durationTypesFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }
}
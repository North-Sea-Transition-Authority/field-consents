package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;

@ExtendWith(MockitoExtension.class)
class WorkAreaFilterFormServiceTest {

  private WorkAreaFilterFormService workAreaFormService;

  @BeforeEach
  void setUp() {
    workAreaFormService = new WorkAreaFilterFormService();
  }

  @Test
  void from_withDefaultWorkAreaFilter() {
    var defaultFilter = ApplicationDataFilterFormTestUtil.getDefaultFilter();
    var workAreaForm = ApplicationDataFilterFormTestUtil.getWorkAreaFormForDefaultFilter();

    assertThat(workAreaFormService.getFromFilter(defaultFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }

  @Test
  void from_withConsentDurationsWorkAreaFilter() {
    var durationTypesFilter = ApplicationDataFilterFormTestUtil.getFilterWithDurationTypes();
    var workAreaForm = ApplicationDataFilterFormTestUtil.getWorkAreaFormForFilterWithDurationTypes();

    assertThat(workAreaFormService.getFromFilter(durationTypesFilter)).usingRecursiveComparison().isEqualTo(workAreaForm);
  }
}

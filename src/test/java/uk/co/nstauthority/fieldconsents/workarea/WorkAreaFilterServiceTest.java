package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;

@ExtendWith(MockitoExtension.class)
class WorkAreaFilterServiceTest {

  private WorkAreaFilterService workAreaFilterService;

  private WorkAreaFilter filter;
  private WorkAreaForm form;

  @BeforeEach
  void setup() {
    workAreaFilterService = new WorkAreaFilterService();
    filter = new WorkAreaFilter();
    form = new WorkAreaForm();
  }

  @Test
  void getConditions_EmptyFilter_AssertEmpty() {
    var conditions = workAreaFilterService.getConditions(filter);
    assertThat(conditions).isEmpty();
  }

  @Test
  void getConditions_StatusesSelected() {
    var status = ApplicationVersionStatus.IN_PROGRESS;
    form.setStatuses(Collections.singletonList(ApplicationVersionStatus.IN_PROGRESS));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.in(Collections.singletonList(status.getEnumName()))
    );
  }

  @Test
  void getConditions_ApplicationTypesSelected() {
    var applicationType = ApplicationType.PRODUCTION;
    form.setApplicationTypes(Collections.singletonList(ApplicationType.PRODUCTION));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        APPLICATIONS.TYPE.in(Collections.singletonList(applicationType.getEnumName()))
    );
  }
}
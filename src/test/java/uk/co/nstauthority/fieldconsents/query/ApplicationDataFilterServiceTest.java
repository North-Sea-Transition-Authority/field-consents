package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_UNIT_ID;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

@ExtendWith(MockitoExtension.class)
class ApplicationDataFilterServiceTest {

  @InjectMocks
  private ApplicationDataFilterService applicationDataFilterService;
  private ApplicationDataFilterForm dataFilterForm;

  @BeforeEach
  void setUp() {
    dataFilterForm = new ApplicationDataFilterForm();
  }

  @Test
  void getConditions_ReferenceNumberSelected() {
    dataFilterForm.setReferenceNumber(APPLICATION_NO);

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATIONS.APPLICATION_NO.cast(String.class).eq(APPLICATION_NO)
    );
  }

  @Test
  void getConditions_StatusesSelected() {
    var status = ApplicationVersionStatus.IN_PROGRESS;
    dataFilterForm.setStatuses(Collections.singletonList(status));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.in(Collections.singletonList(status.getEnumName()))
    );
  }

  @Test
  void getConditions_ApplicationTypesSelected() {
    var applicationType = ApplicationType.PRODUCTION;
    dataFilterForm.setApplicationTypes(Collections.singletonList(applicationType));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATIONS.TYPE.in(Collections.singletonList(applicationType.getEnumName()))
    );
  }

  @Test
  void getConditions_DurationTypesSelected() {
    var durationTypes = ConsentLengthType.LONG_TERM;
    dataFilterForm.setDurationTypes(Collections.singletonList(durationTypes));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        CONSENT_LENGTHS.CONSENT_LENGTH.in(Collections.singletonList(durationTypes.getEnumName()))
    );
  }

  @Test
  void getConditions_OperatorSelected() {
    dataFilterForm.setOperatorId(ORGANISATION_UNIT_ID);

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
    );
  }
}

package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_UNIT_ID;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;

@ExtendWith(MockitoExtension.class)
class SearchFilterServiceTest {

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @InjectMocks
  private SearchFilterService searchFilterService;

  @Test
  void getConditions_withEmptyFilter() {
    assertThat(searchFilterService.getConditions(new ApplicationDataFilterForm())).isEmpty();
  }

  @Test
  void getConditions_withCompleteSearchFilter() {
    var filter = ApplicationDataFilterFormTestUtil.getCompleteApplicationDataFilterForm();
    when(applicationDataFilterService.getConditions(filter)).thenReturn(
        List.of(
            APPLICATIONS.APPLICATION_NO.eq(APPLICATION_NO),
            APPLICATION_VERSIONS.STATUS.in(List.of(ApplicationVersionStatus.SUBMITTED, ApplicationVersionStatus.IN_PROGRESS)),
            APPLICATIONS.TYPE.in(List.of(ApplicationType.PRODUCTION, ApplicationType.VENT)),
            CONSENT_LENGTHS.CONSENT_LENGTH.in(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM)),
            APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
        )
    );

    assertThat(searchFilterService.getConditions(filter))
        .containsExactly(
            APPLICATIONS.APPLICATION_NO.eq(APPLICATION_NO),
            APPLICATION_VERSIONS.STATUS.in(List.of(ApplicationVersionStatus.SUBMITTED, ApplicationVersionStatus.IN_PROGRESS)),
            APPLICATIONS.TYPE.in(List.of(ApplicationType.PRODUCTION, ApplicationType.VENT)),
            CONSENT_LENGTHS.CONSENT_LENGTH.in(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM)),
            APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
        );
  }
}

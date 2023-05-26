package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseStatusFlagServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @InjectMocks
  private CaseStatusFlagService caseStatusFlagService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getCaseStatusFlags_whenCaseOfficerAssigned() {
    applicationVersion.setCaseOfficerWuaId(USER.wuaId());
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsExactly(CaseStatusFlag.CASE_OFFICER_ASSIGNED);
  }

  @Test
  void getCaseStatusFlags_whenCaseOfficerNotAssigned() {
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsExactly(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED);
  }
}

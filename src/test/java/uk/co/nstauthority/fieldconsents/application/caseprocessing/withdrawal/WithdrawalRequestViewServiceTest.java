package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.DUMMY_APP_REF;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.getOpenApplicationWithdrawal;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class WithdrawalRequestViewServiceTest {

  @Mock
  private ApplicationWithdrawalService applicationWithdrawalService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ApplicationService applicationService;

  private WithdrawalRequestViewService withdrawalRequestViewService;


  @BeforeEach
  void setUp() {
    withdrawalRequestViewService = new WithdrawalRequestViewService(
        applicationWithdrawalService,
        energyPortalUserService,
        applicationService
    );
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getWithdrawalRequestView(ApplicationVersion applicationVersion) {
    var openApplicationWithdrawal = getOpenApplicationWithdrawal(applicationVersion);
    var withdrawalRequestView = ApplicationWithdrawalTestUtil.getWithdrawalRequestView();

    when(applicationWithdrawalService.getOpenApplicationWithdrawal(applicationVersion)).thenReturn(openApplicationWithdrawal);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(energyPortalUserService.getByWuaId(any(WebUserAccountId.class))).thenReturn(ENERGY_PORTAL_USER_1);

    assertThat(withdrawalRequestViewService.getWithdrawalRequestView(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(withdrawalRequestView);
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}

package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_3;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class BulkAssignCaseOfficerServiceTest {

  @Mock
  private CaseAssignmentService caseAssignmentService;

  @InjectMocks
  private BulkAssignCaseOfficerService bulkAssignCaseOfficerService;

  @Test
  void assignCaseOfficer() {
    var user = ServiceUserDetailTestUtil.Builder().withWuaId(1L).build();
    var caseOfficer = ServiceUserDetailTestUtil.Builder().withWuaId(2L).build();

    var applicationVersion1 = new ApplicationVersion();
    var applicationVersion2 = new ApplicationVersion();

    var applicationVersion3 = new ApplicationVersion();
    applicationVersion3.setCaseOfficerWuaId(caseOfficer.wuaId());

    bulkAssignCaseOfficerService.assignCaseOfficer(
        List.of(applicationVersion1, applicationVersion2, applicationVersion3),
        caseOfficer,
        user
    );

    verify(caseAssignmentService).assignCaseOfficer(applicationVersion1, caseOfficer, user);
    verify(caseAssignmentService).assignCaseOfficer(applicationVersion2, caseOfficer, user);
  }

  @Test
  void getAvailableCaseOfficers() {
    var energyPortalUsers = List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2, ENERGY_PORTAL_USER_3);
    when(caseAssignmentService.getActiveCaseOfficers()).thenReturn(energyPortalUsers);

    assertThat(bulkAssignCaseOfficerService.getAvailableCaseOfficers()).isEqualTo(energyPortalUsers);
  }

  @Test
  void getNotificationBannerSuccessMessage_single() {
    assertThat(bulkAssignCaseOfficerService.getNotificationBannerSuccessMessage(1, ENERGY_PORTAL_USER_1))
        .isEqualTo("%s has been assigned to 1 application".formatted(ENERGY_PORTAL_USER_1.displayName()));
  }

  @Test
  void getNotificationBannerSuccessMessage_multiple() {
    assertThat(bulkAssignCaseOfficerService.getNotificationBannerSuccessMessage(10, ENERGY_PORTAL_USER_1))
        .isEqualTo("%s has been assigned to 10 applications".formatted(ENERGY_PORTAL_USER_1.displayName()));
  }

}

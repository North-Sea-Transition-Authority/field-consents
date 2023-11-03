package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith({MockitoExtension.class})
class ApplicationUpdateRequestViewServiceTest {

  @Mock
  private Clock clock;

  @Mock
  private ApplicationUpdateService applicationUpdateService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Test
  void getOpenApplicationUpdateRequestView() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    var openApplicationUpdate = ApplicationUpdateTestUtil.getOpenApplicationUpdate(applicationVersion, clock);
    var requesterWuaId = new WebUserAccountId(ENERGY_PORTAL_USER_1.webUserAccountId());

    when(applicationUpdateService.getOpenApplicationUpdate(applicationVersion))
        .thenReturn(openApplicationUpdate);
    when(energyPortalUserService.getByWuaId(requesterWuaId))
        .thenReturn(ENERGY_PORTAL_USER_1);

    var applicationUpdateRequestView = new ApplicationUpdateRequestView(
        "Forename1 Surname1",
        DateUtils.format(openApplicationUpdate.getRequestedDateTime(), DATE_TIME),
        "Text request text",
        DateUtils.format(openApplicationUpdate.getDeadlineDateTime(), DATE_TIME)
    );

    assertThat(applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(applicationUpdateRequestView);
  }
}

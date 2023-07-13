package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationDuplicationService applicationDuplicationService;

  @InjectMocks
  private ApplicationUpdateService applicationUpdateService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setup() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void startApplicationUpdate() {
    var newApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationService.startApplicationUpdate(applicationVersion, USER))
        .thenReturn(newApplicationVersion);

    applicationUpdateService.startApplicationUpdate(applicationVersion, USER);

    verify(applicationDuplicationService, times(1))
        .duplicateApplicationSections(applicationVersion, newApplicationVersion);
  }
}

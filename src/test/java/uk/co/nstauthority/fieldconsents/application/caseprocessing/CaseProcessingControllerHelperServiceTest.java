package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;

@ExtendWith(MockitoExtension.class)
class CaseProcessingControllerHelperServiceTest {

  private static final int APPLICATION_VERSION_ID = 9234;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @InjectMocks
  private CaseProcessingControllerHelperService caseProcessingControllerHelperService;

  private Application application;
  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    application = new Application(1);
    applicationVersion = new ApplicationVersion();
  }

  @Test
  void getApplicationVersionForApplication_isDeleted() {
    applicationVersion.setStatus(ApplicationVersionStatus.DELETED);

    when(applicationVersionService.getApplicationVersionById(APPLICATION_VERSION_ID)).thenReturn(applicationVersion);

    assertThatThrownBy(
        () -> caseProcessingControllerHelperService.getApplicationVersionForApplication(application, APPLICATION_VERSION_ID)
    )
        .isInstanceOf(ResponseStatusException.class)
        .asInstanceOf(type(ResponseStatusException.class))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getApplicationVersionForApplication_belongsToOtherApplication() {
    applicationVersion.setApplication(new Application(836));

    when(applicationVersionService.getApplicationVersionById(APPLICATION_VERSION_ID)).thenReturn(applicationVersion);

    assertThatThrownBy(
        () -> caseProcessingControllerHelperService.getApplicationVersionForApplication(application, APPLICATION_VERSION_ID)
    )
        .isInstanceOf(ResponseStatusException.class)
        .asInstanceOf(type(ResponseStatusException.class))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getApplicationVersionForApplication() {
    applicationVersion.setApplication(application);

    when(applicationVersionService.getApplicationVersionById(APPLICATION_VERSION_ID)).thenReturn(applicationVersion);

    assertThat(caseProcessingControllerHelperService.getApplicationVersionForApplication(application, APPLICATION_VERSION_ID))
        .isEqualTo(applicationVersion);
  }

}
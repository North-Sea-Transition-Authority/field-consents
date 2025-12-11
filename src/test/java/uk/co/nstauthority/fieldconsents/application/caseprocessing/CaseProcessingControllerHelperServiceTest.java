package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationVersionView;

@ExtendWith(MockitoExtension.class)
class CaseProcessingControllerHelperServiceTest {

  private static final int APPLICATION_VERSION_ID = 9234;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationSummaryService applicationSummaryService;

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

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void addSummarySectionsAndVersionOptionsToModelAndView_withMultipleSubmittedApplications(ApplicationType applicationType) {
    var latestApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    latestApplicationVersion.setVersion(2);

    var applicationVersion1 = new ApplicationVersion();
    applicationVersion1.setId(1);
    applicationVersion1.setVersion(1);

    var applicationVersion2 = new ApplicationVersion();
    applicationVersion2.setId(2);
    applicationVersion2.setVersion(2);

    var applicationVersion3 = new ApplicationVersion();
    applicationVersion3.setId(3);
    applicationVersion3.setVersion(3);

    var applicationVersions = List.of(applicationVersion2, applicationVersion3, applicationVersion1);
    when(applicationVersionService.getAllNonDeletedApplicationVersionsByApplicationId(latestApplicationVersion.getApplication().getId()))
        .thenReturn(applicationVersions);

    var modelAndView = new ModelAndView();

    caseProcessingControllerHelperService.addSummarySectionsAndVersionOptionsToModelAndView(latestApplicationVersion, modelAndView, null);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(Map.of(
            "selectedApplicationVersionView", ApplicationVersionView.from(latestApplicationVersion),
            "applicationVersionViews", List.of(
                ApplicationVersionView.from(applicationVersion3),
                ApplicationVersionView.from(applicationVersion2),
                ApplicationVersionView.from(applicationVersion1)
            )
        ));

    verify(applicationSummaryService).addSummarySectionsToModelAndView(latestApplicationVersion, modelAndView, null);
  }

}
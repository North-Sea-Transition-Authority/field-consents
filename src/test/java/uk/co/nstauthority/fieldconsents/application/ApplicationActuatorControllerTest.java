package uk.co.nstauthority.fieldconsents.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.nstauthority.fieldconsents.AbstractActuatorControllerTest;
import uk.co.nstauthority.fieldconsents.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;

class ApplicationActuatorControllerTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_APPLICATION_SUBMITTED_MESSAGE_URL_FORMAT =
      "/actuator/applications/publish-epmq-message/application-version/%s/submitted";

  private static final int APPLICATION_VERSION_ID = 7;

  @MockBean
  private ApplicationVersionService applicationVersionService;

  @MockBean
  private ApplicationSnsService applicationSnsService;

  @Autowired
  private ActuatorConfigurationProperties actuatorConfigurationProperties;

  @SecurityTest
  void publishApplicationSubmittedSnsMessage_notAuthorised() throws Exception {
    mockMvc.perform(post(PUBLISH_APPLICATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(APPLICATION_VERSION_ID))
            .with(httpBasic("admin", "invalidpassword")))
        .andExpect(status().isUnauthorized());

    verify(applicationSnsService, never()).publishApplicationSubmittedSnsMessage(any());
  }

  @SecurityTest
  void publishApplicationSubmittedSnsMessage() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationVersionService.getApplicationVersionById(APPLICATION_VERSION_ID)).thenReturn(applicationVersion);

    mockMvc.perform(post(PUBLISH_APPLICATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(APPLICATION_VERSION_ID))
            .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())))
        .andExpect(status().isOk());

    verify(applicationSnsService).publishApplicationSubmittedSnsMessage(applicationVersion);
  }
}

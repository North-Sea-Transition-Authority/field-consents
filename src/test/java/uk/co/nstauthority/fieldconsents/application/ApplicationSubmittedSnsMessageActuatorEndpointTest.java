package uk.co.nstauthority.fieldconsents.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.fieldconsents.AbstractActuatorControllerTest;
import uk.co.nstauthority.fieldconsents.actuator.ActuatorConfigurationProperties;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;

class ApplicationSubmittedSnsMessageActuatorEndpointTest extends AbstractActuatorControllerTest {

  private static final String PUBLISH_APPLICATION_SUBMITTED_MESSAGE_URL_FORMAT =
      "/actuator/application-submitted-sns-message/%s";

  private static final int APPLICATION_VERSION_ID = 7;

  @MockitoBean
  private ApplicationVersionService applicationVersionService;

  @MockitoBean
  private ApplicationSnsService applicationSnsService;

  @Autowired
  private ActuatorConfigurationProperties actuatorConfigurationProperties;

  @SecurityTest
  void publishMessage_notAuthorised() throws Exception {
    mockMvc.perform(post(PUBLISH_APPLICATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(APPLICATION_VERSION_ID))
            .with(httpBasic("admin", "invalidpassword")))
        .andExpect(status().isUnauthorized());

    verify(applicationSnsService, never()).publishApplicationSubmittedSnsMessage(any());
  }

  @SecurityTest
  void publishMessage() throws Exception {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationVersionService.getApplicationVersionById(APPLICATION_VERSION_ID)).thenReturn(applicationVersion);

    mockMvc.perform(post(PUBLISH_APPLICATION_SUBMITTED_MESSAGE_URL_FORMAT.formatted(APPLICATION_VERSION_ID))
            .with(httpBasic("admin", actuatorConfigurationProperties.adminUserPassword())))
        .andExpect(status().isOk());

    verify(applicationSnsService).publishApplicationSubmittedSnsMessage(applicationVersion);
  }
}

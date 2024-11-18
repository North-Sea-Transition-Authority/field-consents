package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsultationResponseFileController.class)
class ConsultationResponseFileControllerTest extends AbstractApplicationControllerTest {

  private final Class<ConsultationResponseFileController> consultationResponseFileControllerClass = ConsultationResponseFileController.class;
  private final UUID fileId = UUID.randomUUID();
  private final int consultationId = 2;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsultationService consultationService;

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @Captor
  private ArgumentCaptor<Supplier<FieldConsentsFileUsage>> fileUsageSupplierCaptor;

  private Application application;

  private ApplicationVersion applicationVersion;

  private Consultation consultation;

  private String downloadUrl;
  private String deleteUrl;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    application = applicationVersion.getApplication();
    consultation = Consultation.newBuilder().withId(consultationId).build();

    downloadUrl = ReverseRouter.route(on(consultationResponseFileControllerClass).download(application.getId(), consultationId, fileId, null));
    deleteUrl = ReverseRouter.route(on(consultationResponseFileControllerClass).delete(application.getId(), consultationId, fileId, null));

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
  }

  @SecurityTest
  void download_notSignedIn() throws Exception {
    mockMvc.perform(get(downloadUrl))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_doesNotHavePermission() throws Exception {
    mockMvc.perform(get(downloadUrl)
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void delete_notSignedIn() throws Exception {
    mockMvc.perform(post(deleteUrl)
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void delete_doesNotHavePermission() throws Exception {
    mockMvc.perform(post(deleteUrl)
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void download() throws Exception{
    when(applicationService.getApplicationById(application.getId())).thenReturn(application);
    when(consultationService.getConsultationByIdAndApplication(consultationId, application)).thenReturn(consultation);
    when(fileControllerHelperService.download(eq(fileId), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(downloadUrl)
            .with(user(user)))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(ConsultationFileUsage.responseUsageFrom(consultation));
  }

  @Test
  void delete() throws Exception{
    when(applicationService.getApplicationById(application.getId())).thenReturn(application);
    when(consultationService.getConsultationByIdAndApplication(consultationId, application)).thenReturn(consultation);
    when(fileControllerHelperService.delete(eq(fileId), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(post(deleteUrl)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(ConsultationFileUsage.responseUsageFrom(consultation));
  }

}

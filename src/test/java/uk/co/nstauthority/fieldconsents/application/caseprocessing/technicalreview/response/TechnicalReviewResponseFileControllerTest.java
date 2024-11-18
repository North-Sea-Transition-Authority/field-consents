package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = TechnicalReviewResponseFileController.class)
class TechnicalReviewResponseFileControllerTest extends AbstractApplicationControllerTest {

  private final Class<TechnicalReviewResponseFileController> technicalReviewResponseFileControllerClass = TechnicalReviewResponseFileController.class;
  private final UUID fileId = UUID.randomUUID();

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private TechnicalReviewService technicalReviewService;

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @Captor
  private ArgumentCaptor<Supplier<FieldConsentsFileUsage>> fileUsageSupplierCaptor;

  private Application application;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  private String downloadUrl;
  private String deleteUrl;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    application = applicationVersion.getApplication();
    technicalReview = TechnicalReviewTestUtil.getOpenTechnicalReview(applicationVersion);

    downloadUrl = ReverseRouter.route(on(technicalReviewResponseFileControllerClass).download(application.getId(), technicalReview.getId(), fileId, null));
    deleteUrl = ReverseRouter.route(on(technicalReviewResponseFileControllerClass).delete(application.getId(), technicalReview.getId(), fileId, null));

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
    when(technicalReviewService.getTechnicalReviewByApplicationAndId(application, technicalReview.getId())).thenReturn(technicalReview);
    when(fileControllerHelperService.download(eq(fileId), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(downloadUrl)
            .with(user(user)))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(TechnicalReviewFileUsage.responseFrom(technicalReview));
  }

  @Test
  void delete() throws Exception{
    when(applicationService.getApplicationById(application.getId())).thenReturn(application);
    when(technicalReviewService.getTechnicalReviewByApplicationAndId(application, technicalReview.getId())).thenReturn(technicalReview);
    when(fileControllerHelperService.delete(eq(fileId), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(post(deleteUrl)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(TechnicalReviewFileUsage.responseFrom(technicalReview));
  }

}

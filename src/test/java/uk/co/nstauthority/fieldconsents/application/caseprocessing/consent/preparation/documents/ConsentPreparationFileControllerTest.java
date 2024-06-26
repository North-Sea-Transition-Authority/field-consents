package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_ISSUING;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_PREPARATION;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ConsentPreparationFileController.class)
class ConsentPreparationFileControllerTest extends AbstractApplicationControllerTest {

  private final Class<ConsentPreparationFileController> consentPreparationFileControllerClass = ConsentPreparationFileController.class;
  private final UUID fileId = UUID.randomUUID();

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @Captor
  private ArgumentCaptor<Supplier<FieldConsentsFileUsage>> fileUsageSupplierCaptor;

  private Application application;

  private ApplicationVersion applicationVersion;

  private String downloadUrl;
  private String deleteUrl;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    application = applicationVersion.getApplication();

    downloadUrl = ReverseRouter.route(on(consentPreparationFileControllerClass).download(application.getId(), fileId, null));
    deleteUrl = ReverseRouter.route(on(consentPreparationFileControllerClass).delete(application.getId(), fileId, null));

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
  void download_userDoesNotHaveConsentPreparationOrConsentIssuingCaseProcessingActionItems() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());
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
  void delete_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Set.of());
    mockMvc.perform(post(deleteUrl)
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void download() throws Exception {
    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CONSENT_PREPARATION,
        CONSENT_ISSUING
    )).thenReturn(true);
    when(applicationService.getApplicationById(application.getId())).thenReturn(application);
    when(fileControllerHelperService.download(eq(fileId), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(downloadUrl)
            .with(user(user)))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(ApplicationFileUsage.supportingConsentDocumentFrom(application));
  }

  @SecurityTest
  void delete() throws Exception {
    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS
    )).thenReturn(true);
    when(applicationService.getApplicationById(application.getId())).thenReturn(application);
    when(fileControllerHelperService.delete(eq(fileId), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(post(deleteUrl)
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(ApplicationFileUsage.supportingConsentDocumentFrom(application));
  }

}

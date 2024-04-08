package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = ConsentFileController.class)
class ConsentFileControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConsentService consentService;

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @Captor
  private ArgumentCaptor<Supplier<FieldConsentsFileUsage>> fileUsageSupplierCaptor;

  private static final int APPLICATION_ID = 1;
  private static final UUID FILE_ID = UUID.randomUUID();

  private ApplicationVersion applicationVersion;
  private Application application;

  @BeforeEach
  void beforeEach() {
    applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void downloadGeneratedConsentDocument_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class).downloadGeneratedConsentDocument(APPLICATION_ID, FILE_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void downloadGeneratedConsentDocument_userDoesNotHaveViewFcsConsentsApplicationPermission() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.VIEW_FCS_CONSENTS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class).downloadGeneratedConsentDocument(APPLICATION_ID, FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void downloadGeneratedConsentDocument() throws Exception {
    var consent = ConsentTestUtil.newBuilder().build();

    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.VIEW_FCS_CONSENTS))
        .thenReturn(true);
    when(applicationService.getApplicationById(application.getId())).thenReturn(application);
    when(consentService.getConsent(application)).thenReturn(consent);
    when(fileControllerHelperService.download(eq(FILE_ID), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class).downloadGeneratedConsentDocument(APPLICATION_ID, FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(ConsentFileUsage.generatedConsentDocumentFrom(consent));
  }

  @SecurityTest
  void downloadSupportingConsentDocument_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class).downloadSupportingConsentDocument(APPLICATION_ID, FILE_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void downloadSupportingConsentDocument_userDoesNotHaveViewFcsConsentsApplicationPermission() throws Exception {
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.VIEW_FCS_CONSENTS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class).downloadSupportingConsentDocument(APPLICATION_ID, FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void downloadSupportingConsentDocument() throws Exception {
    var consent = ConsentTestUtil.newBuilder().build();

    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.VIEW_FCS_CONSENTS))
        .thenReturn(true);
    when(applicationService.getApplicationById(application.getId())).thenReturn(application);
    when(consentService.getConsent(application)).thenReturn(consent);
    when(fileControllerHelperService.download(eq(FILE_ID), fileUsageSupplierCaptor.capture(), eq(user)))
        .thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class).downloadSupportingConsentDocument(APPLICATION_ID, FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk());

    assertThat(fileUsageSupplierCaptor.getValue().get()).isEqualTo(ConsentFileUsage.supportingConsentDocumentFrom(consent));
  }
}

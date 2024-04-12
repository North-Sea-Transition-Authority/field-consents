package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationDocumentInstanceController.class)
class ApplicationDocumentInstanceControllerTest extends AbstractApplicationControllerTest {

  private static final int APPLICATION_ID = 1;
  private static final UUID DOCUMENT_INSTANCE_ID = UUID.randomUUID();

  @MockBean
  private ApplicationDocumentInstanceService applicationDocumentInstanceService;

  @MockBean
  private ApplicationDocumentInstanceControllerHelperService applicationDocumentInstanceControllerHelperService;

  @MockBean
  private ApplicationDocumentInstanceSectionViewService applicationDocumentInstanceSectionViewService;

  @MockBean
  private DocumentInstanceService documentInstanceService;

  @MockBean
  private ApplicationService applicationService;

  private ApplicationVersion applicationVersion;
  private Application application;

  @BeforeEach
  void beforeEach() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void getViewDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getViewDocumentInstance_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getViewDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstanceSectionSummaryViews = List.of(
        new DocumentInstanceSectionSummaryView(
            "1",
            "Test title 1",
            "Test content 1",
            false,
            Collections.emptyList(),
            DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-1").build(),
            List.of()
        ),
        new DocumentInstanceSectionSummaryView(
            "2",
            "Test title 2",
            "Test content 2",
            false,
            Collections.emptyList(),
            DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-2").build(),
            List.of()
        )
    );

    var documentInstanceSectionsSummaryView = new DocumentInstanceSectionsSummaryView(
        documentInstanceSectionSummaryViews,
        Collections.emptyList()
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(
        applicationDocumentInstanceSectionViewService.getDocumentInstanceSectionsSummaryView(
            application,
            documentInstanceDto,
            true
        )
    ).thenReturn(documentInstanceSectionsSummaryView);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/viewDocumentInstance"))
        .andExpect(model().attribute("pageTitle", documentInstanceDto.documentTemplateDto().title()))
        .andExpect(model().attribute("documentInstanceSectionsSummaryView", documentInstanceSectionsSummaryView))
        .andExpect(model().attribute("previewUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getPreviewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, false))))
        .andExpect(model().attribute("reloadUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getReloadDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID))));
  }

  @SecurityTest
  void getPreviewDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getPreviewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, true))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getPreviewDocumentInstance_userDoesNotHaveConsentPreparationOrConsentIssuingCaseProcessingActionItems() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getPreviewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, true)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedSecurityTest
  @EnumSource(value = CaseProcessingActionItem.class, names = { "CONSENT_PREPARATION", "CONSENT_ISSUING" })
  void getPreviewDocumentInstance_downloadFalse(CaseProcessingActionItem caseProcessingActionItem) throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(caseProcessingActionItem));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(applicationDocumentInstanceService.renderPdf(
        applicationVersion,
        documentInstanceDto,
        PdfRenderingOptions.newBuilder().withPreviewWatermark(true).build())
    ).thenReturn(byteArrayResource);

    var fileName = "PREVIEW %s.pdf".formatted(documentInstanceDto.title());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getPreviewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, false)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(content().bytes(byteArrayResource.getByteArray()))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "filename=\"%s\"".formatted(fileName)));
  }

  @ParameterizedSecurityTest
  @EnumSource(value = CaseProcessingActionItem.class, names = { "CONSENT_PREPARATION", "CONSENT_ISSUING" })
  void getPreviewDocumentInstance_downloadTrue(CaseProcessingActionItem caseProcessingActionItem) throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(caseProcessingActionItem));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(applicationDocumentInstanceService.renderPdf(
        applicationVersion,
        documentInstanceDto,
        PdfRenderingOptions.newBuilder().withPreviewWatermark(true).build())
    ).thenReturn(byteArrayResource);

    var fileName = "PREVIEW %s.pdf".formatted(documentInstanceDto.title());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getPreviewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, true)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(content().bytes(byteArrayResource.getByteArray()))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(fileName)));
  }

  @SecurityTest
  void getReloadDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getReloadDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getReloadDocumentInstance_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getReloadDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getReloadDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var applicationReference = "Test/application/reference";

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getReloadDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/reloadDocumentInstance"))
        .andExpect(model().attribute("documentTitle", documentInstanceDto.documentTemplateDto().title()))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID))));
  }

  @SecurityTest
  void reloadDocumentInstance_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .reloadDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void reloadDocumentInstance_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .reloadDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void reloadDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Document reloaded")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .reloadDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, DOCUMENT_INSTANCE_ID))));

    verify(documentInstanceService).reloadDocumentInstance(documentInstanceDto);
  }
}

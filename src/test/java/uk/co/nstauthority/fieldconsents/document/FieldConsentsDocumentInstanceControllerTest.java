package uk.co.nstauthority.fieldconsents.document;

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
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = FieldConsentsDocumentInstanceController.class)
class FieldConsentsDocumentInstanceControllerTest extends AbstractControllerTest {

  private static final UUID DOCUMENT_INSTANCE_ID = UUID.randomUUID();

  @MockBean
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @MockBean
  private FieldConsentsDocumentInstanceSectionControllerHelperService fieldConsentsDocumentInstanceSectionControllerHelperService;

  @MockBean
  private DocumentInstanceService documentInstanceService;

  @MockBean
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @MockBean
  private ApplicationService applicationService;

  @SecurityTest
  void getViewDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getViewDocumentInstance_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getViewDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstanceSectionSummaryViews = List.of(
        new DocumentInstanceSectionSummaryView(
            0,
            "1",
            "Test title 1",
            "Test content 1",
            false,
            Collections.emptyList(),
            DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-1").build()
        ),
        new DocumentInstanceSectionSummaryView(
            0,
            "2",
            "Test title 2",
            "Test content 2",
            false,
            Collections.emptyList(),
            DocumentInstanceSectionUrlsTestUtil.newBuilderWithUrlSuffix("-2").build()
        )
    );

    var documentInstanceSectionsSummaryView = new DocumentInstanceSectionsSummaryView(
        documentInstanceSectionSummaryViews,
        Collections.emptyList()
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceService.getDocumentInstanceDtoOrThrow(DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(
        fieldConsentsDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            documentInstanceDto,
            true
        )
    ).thenReturn(documentInstanceSectionsSummaryView);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/viewDocumentInstance"))
        .andExpect(model().attribute("pageTitle", documentInstanceDto.documentTemplateDto().title()))
        .andExpect(model().attribute("documentInstanceSectionsSummaryView", documentInstanceSectionsSummaryView))
        .andExpect(model().attribute("previewUrl", ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID))))
        .andExpect(model().attribute("reloadUrl", ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getReloadDocumentInstance(DOCUMENT_INSTANCE_ID))));
  }

  @SecurityTest
  void getPreviewDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getPreviewDocumentInstance_userDoesNotHaveProcessFcsApplicationsOrAuthoriseFcsConsentsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS)))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getPreviewDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.AUTHORISE_FCS_CONSENTS)))
        .thenReturn(true);
    when(documentInstanceService.getDocumentInstanceDtoOrThrow(DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(fieldConsentsDocumentInstanceService.renderPdf(
        documentInstanceDto,
        PdfRenderingOptions.newBuilder().withPreviewWatermark(true).build())
    ).thenReturn(byteArrayResource);

    var fileName = "PREVIEW %s.pdf".formatted(documentInstanceDto.title());

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(content().bytes(byteArrayResource.getByteArray()))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "filename=\"%s\"".formatted(fileName)));
  }

  @SecurityTest
  void getReloadDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getReloadDocumentInstance(DOCUMENT_INSTANCE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getReloadDocumentInstance_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getReloadDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getReloadDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var applicationVersion =
        ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "Test/application/reference";

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceService.getDocumentInstanceDtoOrThrow(DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getReloadDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/reloadDocumentInstance"))
        .andExpect(model().attribute("documentTitle", documentInstanceDto.documentTemplateDto().title()))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID))));
  }

  @SecurityTest
  void reloadDocumentInstance_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .reloadDocumentInstance(DOCUMENT_INSTANCE_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void reloadDocumentInstance_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .reloadDocumentInstance(DOCUMENT_INSTANCE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void reloadDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceService.getDocumentInstanceDtoOrThrow(DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Document reloaded")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .reloadDocumentInstance(DOCUMENT_INSTANCE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID))));

    verify(documentInstanceService).reloadDocumentInstance(documentInstanceDto);
  }
}

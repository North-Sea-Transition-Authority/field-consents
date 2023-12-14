package uk.co.nstauthority.fieldconsents.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = DocumentInstanceSectionController.class)
class DocumentInstanceSectionControllerTest extends AbstractControllerTest {

  private static final UUID DOCUMENT_INSTANCE_SECTION_ID = UUID.randomUUID();

  @MockBean
  private FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @MockBean
  private DocumentInstanceSectionService documentInstanceSectionService;

  @SecurityTest
  void getAddDocumentInstanceSectionBefore_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentInstanceSectionBefore_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAddDocumentInstanceSectionBefore() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attribute("form", DocumentSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));
  }

  @SecurityTest
  void addDocumentInstanceSectionBefore_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentInstanceSectionBefore_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addDocumentInstanceSectionBefore_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "")
            .param("content", "Test content"))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService, never())
        .createDocumentInstanceSection(any(), any(), any(), anyInt());
  }

  @Test
  void addDocumentInstanceSectionBefore_nullParent() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "Test title")
            .param("content", "Test content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        isNull(),
        any(),
        eq(documentInstanceSectionDto.displayOrder())
    );
  }

  @Test
  void addDocumentInstanceSectionBefore_nonNullParent() throws Exception {
    var parentDocumentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var parentId = parentDocumentInstanceSectionDto.id();

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withParentId(parentId)
        .build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);

    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId))
        .thenReturn(parentDocumentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "Test title")
            .param("content", "Test content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        eq(parentDocumentInstanceSectionDto),
        any(),
        eq(documentInstanceSectionDto.displayOrder())
    );
  }

  @SecurityTest
  void getAddDocumentInstanceSectionAfter_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentInstanceSectionAfter_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAddDocumentInstanceSectionAfter() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attribute("form", DocumentSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));
  }

  @SecurityTest
  void addDocumentInstanceSectionAfter_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentInstanceSectionAfter_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addDocumentInstanceSectionAfter_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "")
            .param("content", "Test content"))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService, never())
        .createDocumentInstanceSection(any(), any(), any(), anyInt());
  }

  @Test
  void addDocumentInstanceSectionAfter_nullParent() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "Test title")
            .param("content", "Test content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        isNull(),
        any(),
        eq(documentInstanceSectionDto.displayOrder() + 1)
    );
  }

  @Test
  void addDocumentInstanceSectionAfter_nonNullParent() throws Exception {
    var parentDocumentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var parentId = parentDocumentInstanceSectionDto.id();

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withParentId(parentId)
        .build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);

    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId))
        .thenReturn(parentDocumentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "Test title")
            .param("content", "Test content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        eq(parentDocumentInstanceSectionDto),
        any(),
        eq(documentInstanceSectionDto.displayOrder() + 1)
    );
  }

  @SecurityTest
  void getAddDocumentInstanceSubsection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentInstanceSubsection_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAddDocumentInstanceSubsection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attribute("form", DocumentSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));
  }

  @SecurityTest
  void addDocumentInstanceSubsection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentInstanceSubsection_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addDocumentInstanceSubsection_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "")
            .param("content", "Test content"))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService, never())
        .createDocumentInstanceSection(any(), any(), any(), anyInt());
  }

  @Test
  void addDocumentInstanceSubsection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "Test title")
            .param("content", "Test content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        eq(documentInstanceSectionDto),
        any(),
        eq(1)
    );
  }

  @SecurityTest
  void getEditDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getEditDocumentInstanceSection_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getEditDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attribute("form", DocumentSectionForm.from(documentInstanceSectionDto)))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));
  }

  @SecurityTest
  void editDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .editDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void editDocumentInstanceSection_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .editDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void editDocumentInstanceSection_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .editDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "")
            .param("content", "Test content"))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentInstanceSectionController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("submitButtonText", DocumentInstanceSectionController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService, never())
        .editDocumentInstanceSection(any(), any());
  }

  @Test
  void editDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section saved")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .editDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user))
            .param("title", "Test title")
            .param("content", "Test content"))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(fieldConsentsDocumentInstanceSectionService)
        .editDocumentInstanceSection(eq(documentInstanceSectionDto), any());
  }

  @SecurityTest
  void getRemoveDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getRemoveDocumentInstanceSection_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getRemoveDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/removeDocumentSection"))
        .andExpect(model().attribute("documentSectionDto", documentInstanceSectionDto))
        .andExpect(model().attribute("documentSectionDtoDescendants", documentInstanceSectionDto.descendants()))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));
  }

  @SecurityTest
  void removeDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .removeDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void removeDocumentInstanceSection_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .removeDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void removeDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section removed")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentInstanceSectionController.class)
            .removeDocumentInstanceSection(DOCUMENT_INSTANCE_SECTION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionService)
        .deleteDocumentInstanceSection(documentInstanceSectionDto);
  }
}

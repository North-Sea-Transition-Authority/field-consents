package uk.co.nstauthority.fieldconsents.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = DocumentTemplateSectionController.class)
class DocumentTemplateSectionControllerTest extends AbstractControllerTest {

  private static final UUID DOCUMENT_TEMPLATE_SECTION_ID = UUID.randomUUID();

  @MockBean
  private FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService;

  @MockBean
  private FieldConsentsDocumentTemplateSectionConditionService fieldConsentsDocumentTemplateSectionConditionService;

  @MockBean
  private FieldConsentsDocumentMailMergeFieldService fieldConsentsDocumentMailMergeFieldService;

  @MockBean
  private DocumentTemplateSectionService documentTemplateSectionService;

  @MockBean
  private DocumentTemplateSectionFormValidator documentTemplateSectionFormValidator;

  @SecurityTest
  void getAddDocumentTemplateSectionBefore_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentTemplateSectionBefore_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAddDocumentTemplateSectionBefore() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);
    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attribute("form", DocumentTemplateSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));
  }

  @SecurityTest
  void addDocumentTemplateSectionBefore_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentTemplateSectionBefore_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addDocumentTemplateSectionBefore_invalidForm() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentTemplateSectionFormValidator)
        .validate(any(), any(), any());

    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService, never())
        .createDocumentTemplateSection(any(), any(), any(), anyInt());
  }

  @Test
  void addDocumentTemplateSectionBefore_nullParent() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService).createDocumentTemplateSection(
        eq(documentTemplateSectionDto.documentTemplateDto()),
        isNull(),
        any(),
        eq(documentTemplateSectionDto.displayOrder())
    );
  }

  @Test
  void addDocumentTemplateSectionBefore_nonNullParent() throws Exception {
    var parentDocumentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var parentId = parentDocumentTemplateSectionDto.id();

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withParentId(parentId)
        .build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(parentId))
        .thenReturn(parentDocumentTemplateSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionBefore(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService).createDocumentTemplateSection(
        eq(documentTemplateSectionDto.documentTemplateDto()),
        eq(parentDocumentTemplateSectionDto),
        any(),
        eq(documentTemplateSectionDto.displayOrder())
    );
  }

  @SecurityTest
  void getAddDocumentTemplateSectionAfter_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentTemplateSectionAfter_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAddDocumentTemplateSectionAfter() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);
    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attribute("form", DocumentTemplateSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));
  }

  @SecurityTest
  void addDocumentTemplateSectionAfter_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentTemplateSectionAfter_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addDocumentTemplateSectionAfter_invalidForm() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentTemplateSectionFormValidator)
        .validate(any(), any(), any());

    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService, never())
        .createDocumentTemplateSection(any(), any(), any(), anyInt());
  }

  @Test
  void addDocumentTemplateSectionAfter_nullParent() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService).createDocumentTemplateSection(
        eq(documentTemplateSectionDto.documentTemplateDto()),
        isNull(),
        any(),
        eq(documentTemplateSectionDto.displayOrder() + 1)
    );
  }

  @Test
  void addDocumentTemplateSectionAfter_nonNullParent() throws Exception {
    var parentDocumentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var parentId = parentDocumentTemplateSectionDto.id();

    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder()
        .withParentId(parentId)
        .build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(parentId))
        .thenReturn(parentDocumentTemplateSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSectionAfter(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService).createDocumentTemplateSection(
        eq(documentTemplateSectionDto.documentTemplateDto()),
        eq(parentDocumentTemplateSectionDto),
        any(),
        eq(documentTemplateSectionDto.displayOrder() + 1)
    );
  }

  @SecurityTest
  void getAddDocumentTemplateSubsection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSubsection(DOCUMENT_TEMPLATE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentTemplateSubsection_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSubsection(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAddDocumentTemplateSubsection() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);
    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getAddDocumentTemplateSubsection(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attribute("form", DocumentTemplateSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));
  }

  @SecurityTest
  void addDocumentTemplateSubsection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSubsection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentTemplateSubsection_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSubsection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addDocumentTemplateSubsection_invalidForm() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentTemplateSectionFormValidator)
        .validate(any(), any(), any());

    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSubsection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService, never())
        .createDocumentTemplateSection(any(), any(), any(), anyInt());
  }

  @Test
  void addDocumentTemplateSubsection() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .addDocumentTemplateSubsection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService).createDocumentTemplateSection(
        eq(documentTemplateSectionDto.documentTemplateDto()),
        eq(documentTemplateSectionDto),
        any(),
        eq(1)
    );
  }

  @SecurityTest
  void getEditDocumentTemplateSection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getEditDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getEditDocumentTemplateSection_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getEditDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getEditDocumentTemplateSection() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);
    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getEditDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attribute("form", DocumentTemplateSectionForm.from(documentTemplateSectionDto)))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));
  }

  @SecurityTest
  void editDocumentTemplateSection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .editDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void editDocumentTemplateSection_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .editDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void editDocumentTemplateSection_invalidForm() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    var conditionsFdsSelectMap = Map.of("TEST_MNEMONIC", "Test title");
    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentTemplateSectionFormValidator)
        .validate(any(), any(), any());

    when(fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap())
        .thenReturn(conditionsFdsSelectMap);
    when(fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto))
        .thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .editDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/addOrEditDocumentTemplateSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", DocumentTemplateSectionController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("conditionsFdsSelectMap", conditionsFdsSelectMap))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", DocumentTemplateSectionController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateDto.id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService, never())
        .editDocumentTemplateSection(any(), any());
  }

  @Test
  void editDocumentTemplateSection() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section saved")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .editDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));

    verify(documentTemplateSectionFormValidator).validate(any(), any(), any());

    verify(fieldConsentsDocumentTemplateSectionService)
        .editDocumentTemplateSection(eq(documentTemplateSectionDto), any());
  }

  @SecurityTest
  void getRemoveDocumentTemplateSection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getRemoveDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getRemoveDocumentTemplateSection_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getRemoveDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getRemoveDocumentTemplateSection() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .getRemoveDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/removeDocumentSection"))
        .andExpect(model().attribute("documentSectionDto", documentTemplateSectionDto))
        .andExpect(model().attribute("documentSectionDtoDescendants", documentTemplateSectionDto.descendants()))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));
  }

  @SecurityTest
  void removeDocumentTemplateSection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .removeDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void removeDocumentTemplateSection_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .removeDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void removeDocumentTemplateSection() throws Exception {
    var documentTemplateSectionDto = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(DOCUMENT_TEMPLATE_SECTION_ID))
        .thenReturn(documentTemplateSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section removed")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DocumentTemplateSectionController.class)
            .removeDocumentTemplateSection(DOCUMENT_TEMPLATE_SECTION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))));

    verify(documentTemplateSectionService)
        .deleteDocumentTemplateSection(documentTemplateSectionDto);
  }
}

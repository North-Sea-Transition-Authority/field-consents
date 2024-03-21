package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionForm;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionFormValidator;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationDocumentInstanceSectionController.class)
class ApplicationDocumentInstanceSectionControllerTest extends AbstractApplicationControllerTest {

  private static final int APPLICATION_ID = 1;
  private static final UUID DOCUMENT_INSTANCE_SECTION_ID = UUID.randomUUID();

  @MockBean
  private ApplicationDocumentInstanceSectionControllerHelperService applicationDocumentInstanceSectionControllerHelperService;

  @MockBean
  private DocumentInstanceSectionService documentInstanceSectionService;

  @MockBean
  private DocumentInstanceSectionFormValidator documentInstanceSectionFormValidator;

  @MockBean
  private DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;

  @MockBean
  private DocumentMailMergeFieldControllerHelperService documentMailMergeFieldControllerHelperService;

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
  void getAddDocumentInstanceSectionBefore_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentInstanceSectionBefore_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getAddDocumentInstanceSectionBefore() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attribute("form", DocumentInstanceSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));
  }

  @SecurityTest
  void addDocumentInstanceSectionBefore_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentInstanceSectionBefore_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void addDocumentInstanceSectionBefore_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentInstanceSectionFormValidator)
        .validate(any(), any(), any());

    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService, never())
        .createDocumentInstanceSection(any(), any(), any(), anyInt());
  }

  @SecurityTest
  void addDocumentInstanceSectionBefore_nullParent() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        isNull(),
        any(),
        eq(documentInstanceSectionDto.displayOrder())
    );
  }

  @SecurityTest
  void addDocumentInstanceSectionBefore_nonNullParent() throws Exception {
    var parentDocumentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var parentId = parentDocumentInstanceSectionDto.id();

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withParentId(parentId)
        .build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId))
        .thenReturn(parentDocumentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionBefore(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        eq(parentDocumentInstanceSectionDto),
        any(),
        eq(documentInstanceSectionDto.displayOrder())
    );
  }

  @SecurityTest
  void getAddDocumentInstanceSectionAfter_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentInstanceSectionAfter_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getAddDocumentInstanceSectionAfter() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attribute("form", DocumentInstanceSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));
  }

  @SecurityTest
  void addDocumentInstanceSectionAfter_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentInstanceSectionAfter_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void addDocumentInstanceSectionAfter_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentInstanceSectionFormValidator)
        .validate(any(), any(), any());

    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService, never())
        .createDocumentInstanceSection(any(), any(), any(), anyInt());
  }

  @SecurityTest
  void addDocumentInstanceSectionAfter_nullParent() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        isNull(),
        any(),
        eq(documentInstanceSectionDto.displayOrder() + 1)
    );
  }

  @SecurityTest
  void addDocumentInstanceSectionAfter_nonNullParent() throws Exception {
    var parentDocumentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var parentId = parentDocumentInstanceSectionDto.id();

    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder()
        .withParentId(parentId)
        .build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId))
        .thenReturn(parentDocumentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSectionAfter(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        eq(parentDocumentInstanceSectionDto),
        any(),
        eq(documentInstanceSectionDto.displayOrder() + 1)
    );
  }

  @SecurityTest
  void getAddDocumentInstanceSubsection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getAddDocumentInstanceSubsection_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getAddDocumentInstanceSubsection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getAddDocumentInstanceSubsection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attribute("form", DocumentInstanceSectionForm.empty()))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));
  }

  @SecurityTest
  void addDocumentInstanceSubsection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addDocumentInstanceSubsection_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void addDocumentInstanceSubsection_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentInstanceSectionFormValidator)
        .validate(any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.ADD_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.ADD_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService, never())
        .createDocumentInstanceSection(any(), any(), any(), anyInt());
  }

  @SecurityTest
  void addDocumentInstanceSubsection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section added")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .addDocumentInstanceSubsection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService).createDocumentInstanceSection(
        eq(documentInstanceSectionDto.documentInstanceDto()),
        eq(documentInstanceSectionDto),
        any(),
        eq(1)
    );
  }

  @SecurityTest
  void getEditDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getEditDocumentInstanceSection_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getEditDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);
    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getEditDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attribute("form", DocumentInstanceSectionForm.from(documentInstanceSectionDto)))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));
  }

  @SecurityTest
  void editDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .editDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void editDocumentInstanceSection_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .editDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void editDocumentInstanceSection_invalidForm() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    var applicableDocumentMailMergeFieldViews = List.of(
        new DocumentMailMergeFieldView("TEST_MNEMONIC_1", "Test description 1"),
        new DocumentMailMergeFieldView("TEST_MNEMONIC_2", "Test description 2")
    );

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(2);
      bindingResult.rejectValue("title", "code", "message");
      return bindingResult;
    })
        .when(documentInstanceSectionFormValidator)
        .validate(any(), any(), any());

    when(
        documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(
            documentInstanceDto.documentTemplateDto()
        )
    ).thenReturn(applicableDocumentMailMergeFieldViews);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .editDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageTitle", ApplicationDocumentInstanceSectionController.EDIT_PAGE_TITLE))
        .andExpect(model().attribute("mailMergeFieldViews", applicableDocumentMailMergeFieldViews))
        .andExpect(model().attribute("submitButtonText", ApplicationDocumentInstanceSectionController.EDIT_SUBMIT_BUTTON_TEXT))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceDto.id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService, never())
        .editDocumentInstanceSection(any(), any());
  }

  @SecurityTest
  void editDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section saved")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .editDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionFormValidator).validate(any(), any(), any());

    verify(documentInstanceSectionControllerHelperService)
        .editDocumentInstanceSection(eq(documentInstanceSectionDto), any());
  }

  @SecurityTest
  void getRemoveDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getRemoveDocumentInstanceSection_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getRemoveDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .getRemoveDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/removeDocumentSection"))
        .andExpect(model().attribute("documentSectionDto", documentInstanceSectionDto))
        .andExpect(model().attribute("documentSectionDtoDescendants", documentInstanceSectionDto.descendants()))
        .andExpect(model().attribute("cancelUrl", ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));
  }

  @SecurityTest
  void removeDocumentInstanceSection_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .removeDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void removeDocumentInstanceSection_userDoesNotHaveEditConsentDocumentsCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .removeDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void removeDocumentInstanceSection() throws Exception {
    var documentInstanceSectionDto = DocumentInstanceSectionDtoTestUtil.builder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS));
    when(applicationService.getApplicationById(APPLICATION_ID)).thenReturn(application);
    when(applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(application, DOCUMENT_INSTANCE_SECTION_ID))
        .thenReturn(documentInstanceSectionDto);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Section removed")
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationDocumentInstanceSectionController.class)
            .removeDocumentInstanceSection(APPLICATION_ID, DOCUMENT_INSTANCE_SECTION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
            .getViewDocumentInstance(APPLICATION_ID, documentInstanceSectionDto.documentInstanceDto().id()))));

    verify(documentInstanceSectionService)
        .deleteDocumentInstanceSection(documentInstanceSectionDto);
  }
}

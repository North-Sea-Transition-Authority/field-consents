package uk.co.nstauthority.fieldconsents.document;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSummaryView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = FieldConsentsDocumentTemplateController.class)
class FieldConsentsDocumentTemplateControllerTest extends AbstractControllerTest {

  private static final UUID DOCUMENT_TEMPLATE_ID = UUID.randomUUID();

  @MockBean
  private FieldConsentsDocumentTemplateControllerHelperService fieldConsentsDocumentTemplateControllerHelperService;

  @MockBean
  private FieldConsentsDocumentTemplateSectionControllerHelperService fieldConsentsDocumentTemplateSectionControllerHelperService;

  @MockBean
  private DocumentTemplateService documentTemplateService;

  @SecurityTest
  void getDocumentTemplates_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentTemplateController.class).getDocumentTemplates())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getDocumentTemplates_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentTemplateController.class).getDocumentTemplates()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getDocumentTemplates() throws Exception {
    var documentTemplateSummaryViews = List.of(
        new DocumentTemplateSummaryView("Test title 1", "Test description 1", "test-view-url-1"),
        new DocumentTemplateSummaryView("Test title 2", "Test description 2", "test-view-url-2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(fieldConsentsDocumentTemplateControllerHelperService.getDocumentTemplateSummaryViews())
        .thenReturn(documentTemplateSummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentTemplateController.class).getDocumentTemplates()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/documentTemplates"))
        .andExpect(model().attribute("documentTemplateSummaryViews", documentTemplateSummaryViews));
  }

  @SecurityTest
  void getViewDocumentTemplate_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentTemplateController.class)
            .getViewDocumentTemplate(DOCUMENT_TEMPLATE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getViewDocumentTemplate_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentTemplateController.class)
            .getViewDocumentTemplate(DOCUMENT_TEMPLATE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getViewDocumentTemplate() throws Exception {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSectionSummaryViews = List.of(
        new DocumentTemplateSectionSummaryView(
            "1",
            "Test title 1",
            "Test content 1",
            "TEST_CONDITION_TITLE_1",
            false,
            DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-1").build()
        ),
        new DocumentTemplateSectionSummaryView(
            "1",
            "Test title 2",
            "Test content 2",
            "TEST_CONDITION_TITLE_2",
            false,
            DocumentTemplateSectionUrlsTestUtil.newBuilderWithUrlSuffix("-2").build()
        )
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateService.getDocumentTemplateDtoOrThrow(DOCUMENT_TEMPLATE_ID))
        .thenReturn(documentTemplateDto);
    when(
        fieldConsentsDocumentTemplateSectionControllerHelperService.getDocumentTemplateSectionSummaryViews(
            documentTemplateDto
        )
    ).thenReturn(documentTemplateSectionSummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(FieldConsentsDocumentTemplateController.class)
            .getViewDocumentTemplate(DOCUMENT_TEMPLATE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/viewDocumentTemplate"))
        .andExpect(model().attribute("pageTitle", documentTemplateDto.title()))
        .andExpect(model().attribute("documentTemplateSectionSummaryViews", documentTemplateSectionSummaryViews));
  }
}

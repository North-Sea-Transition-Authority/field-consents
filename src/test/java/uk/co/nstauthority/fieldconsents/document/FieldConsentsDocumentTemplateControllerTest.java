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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionControllerHelperService;
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
  private DocumentTemplateService documentTemplateService;

  @MockBean
  private DocumentTemplateControllerHelperService documentTemplateControllerHelperService;

  @MockBean
  private DocumentTemplateSectionControllerHelperService documentTemplateSectionControllerHelperService;

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

  @Test
  void getDocumentTemplates() throws Exception {
    var documentTemplateSummaryViews = List.of(
        new DocumentTemplateSummaryView("Test title 1", "Test description 1", "test-view-url-1"),
        new DocumentTemplateSummaryView("Test title 2", "Test description 2", "test-view-url-2")
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateControllerHelperService.getDocumentTemplateSummaryViews(FieldConsentsDocumentTemplateController.class))
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

  @Test
  void getViewDocumentTemplate() throws Exception {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentTemplateSectionSummaryViews = List.of(
        new DocumentTemplateSectionSummaryView(
            "1",
            "Test title 1",
            "Test content 1",
            "TEST_CONDITION_TITLE_1",
            false,
            "test-add-section-before-url-1",
            "test-add-section-after-url-1",
            "test-add-subsection-url-1",
            "test-edit-url-1",
            "test-remove-url-1"
        ),
        new DocumentTemplateSectionSummaryView(
            "1",
            "Test title 2",
            "Test content 2",
            "TEST_CONDITION_TITLE_2",
            false,
            "test-add-section-before-url-2",
            "test-add-section-after-url-2",
            "test-add-subsection-url-2",
            "test-edit-url-2",
            "test-remove-url-2"
        )
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateService.getDocumentTemplateDtoOrThrow(DOCUMENT_TEMPLATE_ID))
        .thenReturn(documentTemplateDto);
    when(
        documentTemplateSectionControllerHelperService.getDocumentTemplateSectionSummaryViews(
            documentTemplateDto,
            FieldConsentsDocumentTemplateSectionController.class
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

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
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = DocumentTemplateController.class)
class DocumentTemplateControllerTest extends AbstractControllerTest {

  private static final UUID DOCUMENT_TEMPLATE_ID = UUID.randomUUID();

  @MockBean
  private FieldConsentsDocumentTemplateService fieldConsentsDocumentTemplateService;

  @MockBean
  private FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService;

  @MockBean
  private DocumentTemplateService documentTemplateService;

  @SecurityTest
  void getDocumentTemplates_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class).getDocumentTemplates())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getDocumentTemplates_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class).getDocumentTemplates()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getDocumentTemplates() throws Exception {
    var documentTemplateSummaryViews = List.of(
        DocumentTemplateSummaryView.from(DocumentTemplateDtoTestUtil.builder().build()),
        DocumentTemplateSummaryView.from(DocumentTemplateDtoTestUtil.builder().build())
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(fieldConsentsDocumentTemplateService.getDocumentTemplateSummaryViews())
        .thenReturn(documentTemplateSummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class).getDocumentTemplates()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/documentTemplates"))
        .andExpect(model().attribute("documentTemplateSummaryViews", documentTemplateSummaryViews));
  }

  @SecurityTest
  void getViewDocumentTemplate_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(DOCUMENT_TEMPLATE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getViewDocumentTemplate_userDoesNotHaveManageDocumentTemplatesPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(DOCUMENT_TEMPLATE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getViewDocumentTemplate() throws Exception {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var documentSectionSummaryViews = List.of(
        DocumentSectionSummaryView.from("1", DocumentTemplateSectionDtoTestUtil.builder().build()),
        DocumentSectionSummaryView.from("2", DocumentTemplateSectionDtoTestUtil.builder().build())
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(documentTemplateService.getDocumentTemplateDtoOrThrow(DOCUMENT_TEMPLATE_ID))
        .thenReturn(documentTemplateDto);
    when(fieldConsentsDocumentTemplateSectionService.getDocumentSectionSummaryViews(documentTemplateDto))
        .thenReturn(documentSectionSummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentTemplateController.class)
            .getViewDocumentTemplate(DOCUMENT_TEMPLATE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/viewDocumentTemplate"))
        .andExpect(model().attribute("pageTitle", documentTemplateDto.title()))
        .andExpect(model().attribute("documentSectionSummaryViews", documentSectionSummaryViews));
  }
}

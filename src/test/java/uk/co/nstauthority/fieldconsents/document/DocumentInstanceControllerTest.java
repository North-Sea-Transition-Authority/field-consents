package uk.co.nstauthority.fieldconsents.document;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = DocumentInstanceController.class)
class DocumentInstanceControllerTest extends AbstractControllerTest {

  private static final UUID DOCUMENT_INSTANCE_ID = UUID.randomUUID();

  @MockBean
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @MockBean
  private FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @MockBean
  private DocumentInstanceService documentInstanceService;

  @SecurityTest
  void getViewDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getViewDocumentInstance_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getViewDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentInstanceSectionSummaryViews = List.of(
        DocumentInstanceSectionSummaryView.from(
            "1",
            DocumentInstanceSectionDtoTestUtil.builder().build(),
            "Test content 1"
        ),
        DocumentInstanceSectionSummaryView.from(
            "2",
            DocumentInstanceSectionDtoTestUtil.builder().build(),
            "Test content 2"
        )
    );

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceService.getDocumentInstanceDtoOrThrow(DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViews(documentInstanceDto))
        .thenReturn(documentInstanceSectionSummaryViews);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceController.class)
            .getViewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/document/viewDocumentInstance"))
        .andExpect(model().attribute("pageTitle", documentInstanceDto.documentTemplateDto().title()))
        .andExpect(model().attribute("documentInstanceSectionSummaryViews", documentInstanceSectionSummaryViews))
        .andExpect(model().attribute("previewUrl", ReverseRouter.route(on(DocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID))));
  }

  @SecurityTest
  void getPreviewDocumentInstance_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getPreviewDocumentInstance_userDoesNotHaveProcessFcsApplicationsPermission() throws Exception {
    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getPreviewDocumentInstance() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    when(permissionService.hasPermission(user, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS))).thenReturn(true);
    when(documentInstanceService.getDocumentInstanceDtoOrThrow(DOCUMENT_INSTANCE_ID))
        .thenReturn(documentInstanceDto);
    when(fieldConsentsDocumentInstanceService.renderPdf(documentInstanceDto)).thenReturn(byteArrayResource);

    mockMvc.perform(get(ReverseRouter.route(on(DocumentInstanceController.class)
            .getPreviewDocumentInstance(DOCUMENT_INSTANCE_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(content().bytes(byteArrayResource.getByteArray()))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Document Preview.pdf\""));
  }
}

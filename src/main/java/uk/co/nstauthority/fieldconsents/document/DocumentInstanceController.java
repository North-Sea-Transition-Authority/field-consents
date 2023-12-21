package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.net.HttpHeaders;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-instances")
@HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
public class DocumentInstanceController {

  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;
  private final FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;
  private final DocumentInstanceService documentInstanceService;

  @Autowired
  DocumentInstanceController(
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService,
      FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService,
      DocumentInstanceService documentInstanceService
  ) {
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
    this.fieldConsentsDocumentInstanceSectionService = fieldConsentsDocumentInstanceSectionService;
    this.documentInstanceService = documentInstanceService;
  }

  @GetMapping("/{documentInstanceId}")
  public ModelAndView getViewDocumentInstance(@PathVariable UUID documentInstanceId) {
    var documentInstanceDto = documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId);

    return new ModelAndView("fcs/document/viewDocumentInstance")
        .addObject("pageTitle", documentInstanceDto.documentTemplateDto().title())
        .addObject(
            "documentInstanceSectionSummaryViews",
            fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViews(documentInstanceDto)
        )
        .addObject(
            "previewUrl",
            ReverseRouter.route(on(DocumentInstanceController.class).getPreviewDocumentInstance(documentInstanceId))
        );
  }

  @GetMapping("/{documentInstanceId}/preview")
  public ResponseEntity<?> getPreviewDocumentInstance(@PathVariable UUID documentInstanceId) {
    var documentInstanceDto = documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId);
    var byteArrayResource = fieldConsentsDocumentInstanceService.renderPdf(documentInstanceDto);
    var fileName = "Document Preview.pdf";

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(byteArrayResource.contentLength())
        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName))
        .body(byteArrayResource);
  }
}

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-instances")
@HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
public class DocumentInstanceController {

  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;
  private final FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;
  private final DocumentInstanceService documentInstanceService;
  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ApplicationService applicationService;

  @Autowired
  DocumentInstanceController(
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService,
      FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService,
      DocumentInstanceService documentInstanceService,
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ApplicationService applicationService
  ) {
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
    this.fieldConsentsDocumentInstanceSectionService = fieldConsentsDocumentInstanceSectionService;
    this.documentInstanceService = documentInstanceService;
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.applicationService = applicationService;
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
        )
        .addObject(
            "reloadUrl",
            ReverseRouter.route(on(DocumentInstanceController.class).getReloadDocumentInstance(documentInstanceId))
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

  @GetMapping("/{documentInstanceId}/reload")
  public ModelAndView getReloadDocumentInstance(@PathVariable UUID documentInstanceId) {
    var documentInstanceDto = documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId);
    var applicationVersion =
        documentInstanceLinkingService.getApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    return new ModelAndView("fcs/document/reloadDocumentInstance")
        .addObject("documentTitle", documentInstanceDto.documentTemplateDto().title())
        .addObject("applicationReference", applicationReference)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentInstanceController.class).getViewDocumentInstance(documentInstanceId))
        );
  }

  @PostMapping("/{documentInstanceId}/reload")
  public ModelAndView reloadDocumentInstance(
      @PathVariable UUID documentInstanceId,
      RedirectAttributes redirectAttributes
  ) {
    var documentInstanceDto = documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId);

    documentInstanceService.reloadDocumentInstance(documentInstanceDto);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Document reloaded");

    return ReverseRouter.redirect(on(DocumentInstanceController.class).getViewDocumentInstance(documentInstanceId));
  }
}

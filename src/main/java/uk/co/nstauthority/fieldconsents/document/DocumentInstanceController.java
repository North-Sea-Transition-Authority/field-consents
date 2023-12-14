package uk.co.nstauthority.fieldconsents.document;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-instances")
@HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
public class DocumentInstanceController {

  private final FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;
  private final DocumentInstanceService documentInstanceService;

  @Autowired
  DocumentInstanceController(
      FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService,
      DocumentInstanceService documentInstanceService
  ) {
    this.fieldConsentsDocumentInstanceSectionService = fieldConsentsDocumentInstanceSectionService;
    this.documentInstanceService = documentInstanceService;
  }

  @GetMapping("/{documentInstanceId}")
  public ModelAndView getViewDocumentInstance(@PathVariable UUID documentInstanceId) {
    var documentInstanceDto = documentInstanceService.getDocumentInstanceDtoOrThrow(documentInstanceId);

    return new ModelAndView("fcs/document/viewDocumentInstance")
        .addObject("pageTitle", documentInstanceDto.documentTemplateDto().title())
        .addObject(
            "documentSectionSummaryViews",
            fieldConsentsDocumentInstanceSectionService.getDocumentSectionSummaryViews(documentInstanceDto)
        );
  }
}

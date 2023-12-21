package uk.co.nstauthority.fieldconsents.document;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-templates")
@HasPermission(permissions = RolePermission.MANAGE_DOCUMENT_TEMPLATES)
public class DocumentTemplateController {

  private final FieldConsentsDocumentTemplateService fieldConsentsDocumentTemplateService;
  private final FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService;
  private final DocumentTemplateService documentTemplateService;

  @Autowired
  DocumentTemplateController(
      FieldConsentsDocumentTemplateService fieldConsentsDocumentTemplateService,
      FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService,
      DocumentTemplateService documentTemplateService
  ) {
    this.fieldConsentsDocumentTemplateService = fieldConsentsDocumentTemplateService;
    this.fieldConsentsDocumentTemplateSectionService = fieldConsentsDocumentTemplateSectionService;
    this.documentTemplateService = documentTemplateService;
  }

  @GetMapping
  public ModelAndView getDocumentTemplates() {
    return new ModelAndView("fcs/document/documentTemplates")
        .addObject(
            "documentTemplateSummaryViews",
            fieldConsentsDocumentTemplateService.getDocumentTemplateSummaryViews()
        );
  }

  @GetMapping("/{documentTemplateId}")
  public ModelAndView getViewDocumentTemplate(@PathVariable UUID documentTemplateId) {
    var documentTemplateDto = documentTemplateService.getDocumentTemplateDtoOrThrow(documentTemplateId);

    return new ModelAndView("fcs/document/viewDocumentTemplate")
        .addObject("pageTitle", documentTemplateDto.title())
        .addObject(
            "documentTemplateSectionSummaryViews",
            fieldConsentsDocumentTemplateSectionService.getDocumentTemplateSectionSummaryViews(documentTemplateDto)
        );
  }
}

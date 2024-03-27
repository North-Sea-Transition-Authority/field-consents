package uk.co.nstauthority.fieldconsents.document.template;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-templates")
@HasPermission(permissions = RolePermission.MANAGE_DOCUMENT_TEMPLATES)
public class DocumentTemplateController {

  private final FieldConsentsDocumentTemplateControllerHelperService fieldConsentsDocumentTemplateControllerHelperService;
  private final FieldConsentsDocumentTemplateSectionControllerHelperService
      fieldConsentsDocumentTemplateSectionControllerHelperService;
  private final DocumentTemplateService documentTemplateService;

  DocumentTemplateController(
      FieldConsentsDocumentTemplateControllerHelperService fieldConsentsDocumentTemplateControllerHelperService,
      FieldConsentsDocumentTemplateSectionControllerHelperService fieldConsentsDocumentTemplateSectionControllerHelperService,
      DocumentTemplateService documentTemplateService
  ) {
    this.fieldConsentsDocumentTemplateControllerHelperService = fieldConsentsDocumentTemplateControllerHelperService;
    this.fieldConsentsDocumentTemplateSectionControllerHelperService =
        fieldConsentsDocumentTemplateSectionControllerHelperService;
    this.documentTemplateService = documentTemplateService;
  }

  @GetMapping
  public ModelAndView getDocumentTemplates() {
    var documentTemplateSummaryViews = fieldConsentsDocumentTemplateControllerHelperService.getDocumentTemplateSummaryViews();

    return new ModelAndView("fcs/document/template/documentTemplates")
        .addObject("documentTemplateSummaryViews", documentTemplateSummaryViews);
  }

  @GetMapping("/{documentTemplateId}")
  public ModelAndView getViewDocumentTemplate(@PathVariable UUID documentTemplateId) {
    var documentTemplateDto = documentTemplateService.getDocumentTemplateDtoOrThrow(documentTemplateId);

    var topLevelDocumentTemplateSectionSummaryViews = fieldConsentsDocumentTemplateSectionControllerHelperService
        .getTopLevelDocumentTemplateSectionSummaryViews(documentTemplateDto);

    return new ModelAndView("fcs/document/template/viewDocumentTemplate")
        .addObject("pageTitle", documentTemplateDto.title())
        .addObject("topLevelDocumentTemplateSectionSummaryViews", topLevelDocumentTemplateSectionSummaryViews);
  }
}

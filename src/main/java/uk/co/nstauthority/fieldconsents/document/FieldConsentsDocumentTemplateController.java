package uk.co.nstauthority.fieldconsents.document;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateController;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-templates")
@HasPermission(permissions = RolePermission.MANAGE_DOCUMENT_TEMPLATES)
public class FieldConsentsDocumentTemplateController implements DocumentTemplateController {

  private final DocumentTemplateService documentTemplateService;
  private final DocumentTemplateControllerHelperService documentTemplateControllerHelperService;
  private final DocumentTemplateSectionControllerHelperService documentTemplateSectionControllerHelperService;

  FieldConsentsDocumentTemplateController(
      DocumentTemplateService documentTemplateService,
      DocumentTemplateControllerHelperService documentTemplateControllerHelperService,
      DocumentTemplateSectionControllerHelperService documentTemplateSectionControllerHelperService
  ) {
    this.documentTemplateService = documentTemplateService;
    this.documentTemplateControllerHelperService = documentTemplateControllerHelperService;
    this.documentTemplateSectionControllerHelperService = documentTemplateSectionControllerHelperService;
  }

  @GetMapping
  public ModelAndView getDocumentTemplates() {
    return new ModelAndView("fcs/document/documentTemplates")
        .addObject(
            "documentTemplateSummaryViews",
            documentTemplateControllerHelperService.getDocumentTemplateSummaryViews(FieldConsentsDocumentTemplateController.class)
        );
  }

  @GetMapping("/{documentTemplateId}")
  @Override
  public ModelAndView getViewDocumentTemplate(@PathVariable UUID documentTemplateId) {
    var documentTemplateDto = documentTemplateService.getDocumentTemplateDtoOrThrow(documentTemplateId);

    return new ModelAndView("fcs/document/viewDocumentTemplate")
        .addObject("pageTitle", documentTemplateDto.title())
        .addObject(
            "documentTemplateSectionSummaryViews",
            documentTemplateSectionControllerHelperService.getDocumentTemplateSectionSummaryViews(
                documentTemplateDto,
                FieldConsentsDocumentTemplateSectionController.class
            )
        );
  }
}

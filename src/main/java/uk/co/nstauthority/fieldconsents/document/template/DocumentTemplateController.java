package uk.co.nstauthority.fieldconsents.document.template;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.authorisation.role.HasRegulatorRole;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Controller
@RequestMapping("/document-templates")
@HasRegulatorRole(Role.DOCUMENT_TEMPLATE_MANAGER)
public class DocumentTemplateController {

  private final FieldConsentsDocumentTemplateViewService fieldConsentsDocumentTemplateViewService;
  private final FieldConsentsDocumentTemplateSectionViewService fieldConsentsDocumentTemplateSectionViewService;
  private final DocumentTemplateService documentTemplateService;

  DocumentTemplateController(
      FieldConsentsDocumentTemplateViewService fieldConsentsDocumentTemplateViewService,
      FieldConsentsDocumentTemplateSectionViewService fieldConsentsDocumentTemplateSectionViewService,
      DocumentTemplateService documentTemplateService
  ) {
    this.fieldConsentsDocumentTemplateViewService = fieldConsentsDocumentTemplateViewService;
    this.fieldConsentsDocumentTemplateSectionViewService = fieldConsentsDocumentTemplateSectionViewService;
    this.documentTemplateService = documentTemplateService;
  }

  @GetMapping
  public ModelAndView getDocumentTemplates() {
    var documentTemplateSummaryViews = fieldConsentsDocumentTemplateViewService.getDocumentTemplateSummaryViews();

    return new ModelAndView("fcs/document/template/documentTemplates")
        .addObject("documentTemplateSummaryViews", documentTemplateSummaryViews);
  }

  @GetMapping("/{documentTemplateId}")
  public ModelAndView getViewDocumentTemplate(@PathVariable UUID documentTemplateId) {
    var documentTemplateDto = documentTemplateService.getDocumentTemplateDtoOrThrow(documentTemplateId);

    var topLevelDocumentTemplateSectionSummaryViews = fieldConsentsDocumentTemplateSectionViewService
        .getTopLevelDocumentTemplateSectionSummaryViews(documentTemplateDto);

    return new ModelAndView("fcs/document/template/viewDocumentTemplate")
        .addObject("pageTitle", documentTemplateDto.title())
        .addObject("topLevelDocumentTemplateSectionSummaryViews", topLevelDocumentTemplateSectionSummaryViews)
        .addObject("backLinkUrl", ReverseRouter.route(on(DocumentTemplateController.class).getDocumentTemplates()));
  }
}

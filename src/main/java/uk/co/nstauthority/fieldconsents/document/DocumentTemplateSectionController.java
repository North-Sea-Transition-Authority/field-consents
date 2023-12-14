package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-templates/section/{documentTemplateSectionId}")
@HasPermission(permissions = RolePermission.MANAGE_DOCUMENT_TEMPLATES)
public class DocumentTemplateSectionController {

  static final String ADD_PAGE_TITLE = "Add section";
  static final String ADD_SUBMIT_BUTTON_TEXT = "Add";
  static final String EDIT_PAGE_TITLE = "Edit section";
  static final String EDIT_SUBMIT_BUTTON_TEXT = "Save";

  private final FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService;
  private final DocumentTemplateSectionService documentTemplateSectionService;

  @Autowired
  DocumentTemplateSectionController(
      FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService,
      DocumentTemplateSectionService documentTemplateSectionService
  ) {
    this.fieldConsentsDocumentTemplateSectionService = fieldConsentsDocumentTemplateSectionService;
    this.documentTemplateSectionService = documentTemplateSectionService;
  }

  @GetMapping("/add-before")
  public ModelAndView getAddDocumentTemplateSectionBefore(@PathVariable UUID documentTemplateSectionId) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, DocumentSectionForm.empty());
  }

  @PostMapping("/add-before")
  public ModelAndView addDocumentTemplateSectionBefore(
      @PathVariable UUID documentTemplateSectionId,
      @Valid @ModelAttribute("form") DocumentSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    var parentId = documentTemplateSectionDto.parentId();
    var parentDocumentTemplateSectionDto = parentId != null
        ? documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(parentId)
        : null;

    return addDocumentSection(
        documentTemplateSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        parentDocumentTemplateSectionDto,
        documentTemplateSectionDto.displayOrder()
    );
  }

  @GetMapping("/add-after")
  public ModelAndView getAddDocumentTemplateSectionAfter(@PathVariable UUID documentTemplateSectionId) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, DocumentSectionForm.empty());
  }

  @PostMapping("/add-after")
  public ModelAndView addDocumentTemplateSectionAfter(
      @PathVariable UUID documentTemplateSectionId,
      @Valid @ModelAttribute("form") DocumentSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    var parentId = documentTemplateSectionDto.parentId();
    var parentDocumentTemplateSectionDto = parentId != null
        ? documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(parentId)
        : null;

    return addDocumentSection(
        documentTemplateSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        parentDocumentTemplateSectionDto,
        documentTemplateSectionDto.displayOrder() + 1
    );
  }

  @GetMapping("/add-subsection")
  public ModelAndView getAddDocumentTemplateSubsection(@PathVariable UUID documentTemplateSectionId) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, DocumentSectionForm.empty());
  }

  @PostMapping("/add-subsection")
  public ModelAndView addDocumentTemplateSubsection(
      @PathVariable UUID documentTemplateSectionId,
      @Valid @ModelAttribute("form") DocumentSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    return addDocumentSection(
        documentTemplateSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        documentTemplateSectionDto,
        1
    );
  }

  private ModelAndView getAddDocumentTemplateSectionModelAndView(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentSectionForm form
  ) {
    return new ModelAndView("fcs/document/addOrEditDocumentSection")
        .addObject("form", form)
        .addObject("pageTitle", ADD_PAGE_TITLE)
        .addObject("submitButtonText", ADD_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentTemplateController.class)
                .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))
        );
  }

  private ModelAndView addDocumentSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      @Nullable DocumentTemplateSectionDto parentDto,
      int displayOrder
  ) {
    if (bindingResult.hasErrors()) {
      return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, form);
    }

    fieldConsentsDocumentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateSectionDto.documentTemplateDto(),
        parentDto,
        form,
        displayOrder
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section added");

    return ReverseRouter.redirect(on(DocumentTemplateController.class)
        .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()));
  }

  @GetMapping("/edit")
  public ModelAndView getEditDocumentTemplateSection(@PathVariable UUID documentTemplateSectionId) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);
    var form = DocumentSectionForm.from(documentTemplateSectionDto);

    return getEditDocumentTemplateSectionModelAndView(documentTemplateSectionDto, form);
  }

  @PostMapping("/edit")
  public ModelAndView editDocumentTemplateSection(
      @PathVariable UUID documentTemplateSectionId,
      @Valid @ModelAttribute("form") DocumentSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    if (bindingResult.hasErrors()) {
      return getEditDocumentTemplateSectionModelAndView(documentTemplateSectionDto, form);
    }

    fieldConsentsDocumentTemplateSectionService.editDocumentTemplateSection(documentTemplateSectionDto, form);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section saved");

    return ReverseRouter.redirect(on(DocumentTemplateController.class)
        .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()));
  }

  private ModelAndView getEditDocumentTemplateSectionModelAndView(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentSectionForm form
  ) {
    return new ModelAndView("fcs/document/addOrEditDocumentSection")
        .addObject("form", form)
        .addObject("pageTitle", EDIT_PAGE_TITLE)
        .addObject("submitButtonText", EDIT_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentTemplateController.class)
                .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))
        );
  }

  @GetMapping("/remove")
  public ModelAndView getRemoveDocumentTemplateSection(@PathVariable UUID documentTemplateSectionId) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    return new ModelAndView("fcs/document/removeDocumentSection")
        .addObject("documentSectionDto", documentTemplateSectionDto)
        .addObject("documentSectionDtoDescendants", documentTemplateSectionDto.descendants())
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentTemplateController.class)
                .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()))
        );
  }

  @PostMapping("/remove")
  public ModelAndView removeDocumentTemplateSection(
      @PathVariable UUID documentTemplateSectionId,
      RedirectAttributes redirectAttributes
  ) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    documentTemplateSectionService.deleteDocumentTemplateSection(documentTemplateSectionDto);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section removed");

    return ReverseRouter.redirect(on(DocumentTemplateController.class)
        .getViewDocumentTemplate(documentTemplateSectionDto.documentTemplateDto().id()));
  }
}

package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.annotation.Nullable;
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
  private final FieldConsentsDocumentTemplateSectionConditionService fieldConsentsDocumentTemplateSectionConditionService;
  private final FieldConsentsDocumentMailMergeFieldService fieldConsentsDocumentMailMergeFieldService;
  private final DocumentTemplateSectionService documentTemplateSectionService;
  private final DocumentTemplateSectionFormValidator documentTemplateSectionFormValidator;

  @Autowired
  DocumentTemplateSectionController(
      FieldConsentsDocumentTemplateSectionService fieldConsentsDocumentTemplateSectionService,
      FieldConsentsDocumentTemplateSectionConditionService fieldConsentsDocumentTemplateSectionConditionService,
      FieldConsentsDocumentMailMergeFieldService fieldConsentsDocumentMailMergeFieldService,
      DocumentTemplateSectionService documentTemplateSectionService,
      DocumentTemplateSectionFormValidator documentTemplateSectionFormValidator
  ) {
    this.fieldConsentsDocumentTemplateSectionService = fieldConsentsDocumentTemplateSectionService;
    this.fieldConsentsDocumentTemplateSectionConditionService = fieldConsentsDocumentTemplateSectionConditionService;
    this.fieldConsentsDocumentMailMergeFieldService = fieldConsentsDocumentMailMergeFieldService;
    this.documentTemplateSectionService = documentTemplateSectionService;
    this.documentTemplateSectionFormValidator = documentTemplateSectionFormValidator;
  }

  @GetMapping("/add-before")
  public ModelAndView getAddDocumentTemplateSectionBefore(@PathVariable UUID documentTemplateSectionId) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);

    return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, DocumentTemplateSectionForm.empty());
  }

  @PostMapping("/add-before")
  public ModelAndView addDocumentTemplateSectionBefore(
      @PathVariable UUID documentTemplateSectionId,
      @ModelAttribute("form") DocumentTemplateSectionForm form,
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

    return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, DocumentTemplateSectionForm.empty());
  }

  @PostMapping("/add-after")
  public ModelAndView addDocumentTemplateSectionAfter(
      @PathVariable UUID documentTemplateSectionId,
      @ModelAttribute("form") DocumentTemplateSectionForm form,
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

    return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, DocumentTemplateSectionForm.empty());
  }

  @PostMapping("/add-subsection")
  public ModelAndView addDocumentTemplateSubsection(
      @PathVariable UUID documentTemplateSectionId,
      @ModelAttribute("form") DocumentTemplateSectionForm form,
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
      DocumentTemplateSectionForm form
  ) {
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    return new ModelAndView("fcs/document/addOrEditDocumentTemplateSection")
        .addObject("form", form)
        .addObject("pageTitle", ADD_PAGE_TITLE)
        .addObject(
            "conditionsFdsSelectMap",
            fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap(documentTemplateDto)
        )
        .addObject(
            "mailMergeFieldViews",
            fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto)
        )
        .addObject("submitButtonText", ADD_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentTemplateController.class).getViewDocumentTemplate(documentTemplateDto.id()))
        );
  }

  private ModelAndView addDocumentSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentTemplateSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      @Nullable DocumentTemplateSectionDto parentDto,
      int displayOrder
  ) {
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAddDocumentTemplateSectionModelAndView(documentTemplateSectionDto, form);
    }

    fieldConsentsDocumentTemplateSectionService.createDocumentTemplateSection(
        documentTemplateDto,
        parentDto,
        form,
        displayOrder
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section added");

    return ReverseRouter.redirect(on(DocumentTemplateController.class)
        .getViewDocumentTemplate(documentTemplateDto.id()));
  }

  @GetMapping("/edit")
  public ModelAndView getEditDocumentTemplateSection(@PathVariable UUID documentTemplateSectionId) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);
    var form = DocumentTemplateSectionForm.from(documentTemplateSectionDto);

    return getEditDocumentTemplateSectionModelAndView(documentTemplateSectionDto, form);
  }

  @PostMapping("/edit")
  public ModelAndView editDocumentTemplateSection(
      @PathVariable UUID documentTemplateSectionId,
      @ModelAttribute("form") DocumentTemplateSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentTemplateSectionDto =
        documentTemplateSectionService.getDocumentTemplateSectionDtoOrThrow(documentTemplateSectionId);
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    documentTemplateSectionFormValidator.validate(form, documentTemplateDto, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditDocumentTemplateSectionModelAndView(documentTemplateSectionDto, form);
    }

    fieldConsentsDocumentTemplateSectionService.editDocumentTemplateSection(documentTemplateSectionDto, form);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section saved");

    return ReverseRouter.redirect(on(DocumentTemplateController.class)
        .getViewDocumentTemplate(documentTemplateDto.id()));
  }

  private ModelAndView getEditDocumentTemplateSectionModelAndView(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      DocumentTemplateSectionForm form
  ) {
    var documentTemplateDto = documentTemplateSectionDto.documentTemplateDto();

    return new ModelAndView("fcs/document/addOrEditDocumentTemplateSection")
        .addObject("form", form)
        .addObject("pageTitle", EDIT_PAGE_TITLE)
        .addObject(
            "conditionsFdsSelectMap",
            fieldConsentsDocumentTemplateSectionConditionService.getConditionsFdsSelectMap(documentTemplateDto)
        )
        .addObject(
            "mailMergeFieldViews",
            fieldConsentsDocumentMailMergeFieldService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto)
        )
        .addObject("submitButtonText", EDIT_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentTemplateController.class).getViewDocumentTemplate(documentTemplateDto.id()))
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

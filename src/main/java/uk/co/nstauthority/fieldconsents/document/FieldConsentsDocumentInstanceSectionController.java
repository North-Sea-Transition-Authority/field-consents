package uk.co.nstauthority.fieldconsents.document;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.annotation.Nullable;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionControllerHelperService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionForm;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionFormValidator;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldControllerHelperService;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-instances/section/{documentInstanceSectionId}")
@HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
public class FieldConsentsDocumentInstanceSectionController {

  static final String ADD_PAGE_TITLE = "Add section";
  static final String ADD_SUBMIT_BUTTON_TEXT = "Add";
  static final String EDIT_PAGE_TITLE = "Edit section";
  static final String EDIT_SUBMIT_BUTTON_TEXT = "Save";

  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentInstanceSectionFormValidator documentInstanceSectionFormValidator;
  private final DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;
  private final DocumentMailMergeFieldControllerHelperService documentMailMergeFieldControllerHelperService;

  FieldConsentsDocumentInstanceSectionController(
      DocumentInstanceSectionService documentInstanceSectionService,
      DocumentInstanceSectionFormValidator documentInstanceSectionFormValidator,
      DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService,
      DocumentMailMergeFieldControllerHelperService documentMailMergeFieldControllerHelperService
  ) {
    this.documentInstanceSectionService = documentInstanceSectionService;
    this.documentInstanceSectionFormValidator = documentInstanceSectionFormValidator;
    this.documentInstanceSectionControllerHelperService = documentInstanceSectionControllerHelperService;
    this.documentMailMergeFieldControllerHelperService = documentMailMergeFieldControllerHelperService;
  }

  @GetMapping("/add-before")
  public ModelAndView getAddDocumentInstanceSectionBefore(@PathVariable UUID documentInstanceSectionId) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    return getAddDocumentInstanceSectionModelAndView(documentInstanceSectionDto, DocumentInstanceSectionForm.empty());
  }

  @PostMapping("/add-before")
  public ModelAndView addDocumentInstanceSectionBefore(
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    var parentId = documentInstanceSectionDto.parentId();
    var parentDocumentInstanceSectionDto = parentId != null
        ? documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId)
        : null;

    return addDocumentSection(
        documentInstanceSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        parentDocumentInstanceSectionDto,
        documentInstanceSectionDto.displayOrder()
    );
  }

  @GetMapping("/add-after")
  public ModelAndView getAddDocumentInstanceSectionAfter(@PathVariable UUID documentInstanceSectionId) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    return getAddDocumentInstanceSectionModelAndView(documentInstanceSectionDto, DocumentInstanceSectionForm.empty());
  }

  @PostMapping("/add-after")
  public ModelAndView addDocumentInstanceSectionAfter(
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    var parentId = documentInstanceSectionDto.parentId();
    var parentDocumentInstanceSectionDto = parentId != null
        ? documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId)
        : null;

    return addDocumentSection(
        documentInstanceSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        parentDocumentInstanceSectionDto,
        documentInstanceSectionDto.displayOrder() + 1
    );
  }

  @GetMapping("/add-subsection")
  public ModelAndView getAddDocumentInstanceSubsection(@PathVariable UUID documentInstanceSectionId) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    return getAddDocumentInstanceSectionModelAndView(documentInstanceSectionDto, DocumentInstanceSectionForm.empty());
  }

  @PostMapping("/add-subsection")
  public ModelAndView addDocumentInstanceSubsection(
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    return addDocumentSection(
        documentInstanceSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        documentInstanceSectionDto,
        1
    );
  }

  private ModelAndView getAddDocumentInstanceSectionModelAndView(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    return new ModelAndView("fcs/document/addOrEditDocumentInstanceSection")
        .addObject("form", form)
        .addObject("pageTitle", ADD_PAGE_TITLE)
        .addObject(
            "mailMergeFieldViews",
            documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto)
        )
        .addObject("submitButtonText", ADD_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
                .getViewDocumentInstance(documentInstanceDto.id()))
        );
  }

  private ModelAndView addDocumentSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      @Nullable DocumentInstanceSectionDto parentDto,
      int displayOrder
  ) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAddDocumentInstanceSectionModelAndView(documentInstanceSectionDto, form);
    }

    documentInstanceSectionControllerHelperService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form,
        displayOrder
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section added");

    return ReverseRouter.redirect(on(FieldConsentsDocumentInstanceController.class)
        .getViewDocumentInstance(documentInstanceDto.id()));
  }

  @GetMapping("/edit")
  public ModelAndView getEditDocumentInstanceSection(@PathVariable UUID documentInstanceSectionId) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);
    var form = DocumentInstanceSectionForm.from(documentInstanceSectionDto);

    return getEditDocumentInstanceSectionModelAndView(documentInstanceSectionDto, form);
  }

  @PostMapping("/edit")
  public ModelAndView editDocumentInstanceSection(
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditDocumentInstanceSectionModelAndView(documentInstanceSectionDto, form);
    }

    documentInstanceSectionControllerHelperService.editDocumentInstanceSection(documentInstanceSectionDto, form);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section saved");

    return ReverseRouter.redirect(on(FieldConsentsDocumentInstanceController.class)
        .getViewDocumentInstance(documentInstanceDto.id()));
  }

  private ModelAndView getEditDocumentInstanceSectionModelAndView(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    return new ModelAndView("fcs/document/addOrEditDocumentInstanceSection")
        .addObject("form", form)
        .addObject("pageTitle", EDIT_PAGE_TITLE)
        .addObject("submitButtonText", EDIT_SUBMIT_BUTTON_TEXT)
        .addObject(
            "mailMergeFieldViews",
            documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto)
        )
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
                .getViewDocumentInstance(documentInstanceDto.id()))
        );
  }

  @GetMapping("/remove")
  public ModelAndView getRemoveDocumentInstanceSection(@PathVariable UUID documentInstanceSectionId) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    return new ModelAndView("fcs/document/removeDocumentSection")
        .addObject("documentSectionDto", documentInstanceSectionDto)
        .addObject("documentSectionDtoDescendants", documentInstanceSectionDto.descendants())
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(FieldConsentsDocumentInstanceController.class)
                .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))
        );
  }

  @PostMapping("/remove")
  public ModelAndView removeDocumentInstanceSection(
      @PathVariable UUID documentInstanceSectionId,
      RedirectAttributes redirectAttributes
  ) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    documentInstanceSectionService.deleteDocumentInstanceSection(documentInstanceSectionDto);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section removed");

    return ReverseRouter.redirect(on(FieldConsentsDocumentInstanceController.class)
        .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()));
  }
}

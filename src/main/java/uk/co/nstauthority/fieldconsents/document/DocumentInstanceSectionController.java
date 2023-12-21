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
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/document-instances/section/{documentInstanceSectionId}")
@HasPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
public class DocumentInstanceSectionController {

  static final String ADD_PAGE_TITLE = "Add section";
  static final String ADD_SUBMIT_BUTTON_TEXT = "Add";
  static final String EDIT_PAGE_TITLE = "Edit section";
  static final String EDIT_SUBMIT_BUTTON_TEXT = "Save";

  private final FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;
  private final DocumentInstanceSectionService documentInstanceSectionService;

  @Autowired
  DocumentInstanceSectionController(
      FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService,
      DocumentInstanceSectionService documentInstanceSectionService
  ) {
    this.fieldConsentsDocumentInstanceSectionService = fieldConsentsDocumentInstanceSectionService;
    this.documentInstanceSectionService = documentInstanceSectionService;
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
      @Valid @ModelAttribute("form") DocumentInstanceSectionForm form,
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
      @Valid @ModelAttribute("form") DocumentInstanceSectionForm form,
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
      @Valid @ModelAttribute("form") DocumentInstanceSectionForm form,
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
    return new ModelAndView("fcs/document/addOrEditDocumentInstanceSection")
        .addObject("form", form)
        .addObject("pageTitle", ADD_PAGE_TITLE)
        .addObject("submitButtonText", ADD_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentInstanceController.class)
                .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))
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
    if (bindingResult.hasErrors()) {
      return getAddDocumentInstanceSectionModelAndView(documentInstanceSectionDto, form);
    }

    fieldConsentsDocumentInstanceSectionService.createDocumentInstanceSection(
        documentInstanceSectionDto.documentInstanceDto(),
        parentDto,
        form,
        displayOrder
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section added");

    return ReverseRouter.redirect(on(DocumentInstanceController.class)
        .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()));
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
      @Valid @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var documentInstanceSectionDto =
        documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(documentInstanceSectionId);

    if (bindingResult.hasErrors()) {
      return getEditDocumentInstanceSectionModelAndView(documentInstanceSectionDto, form);
    }

    fieldConsentsDocumentInstanceSectionService.editDocumentInstanceSection(documentInstanceSectionDto, form);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section saved");

    return ReverseRouter.redirect(on(DocumentInstanceController.class)
        .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()));
  }

  private ModelAndView getEditDocumentInstanceSectionModelAndView(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    return new ModelAndView("fcs/document/addOrEditDocumentInstanceSection")
        .addObject("form", form)
        .addObject("pageTitle", EDIT_PAGE_TITLE)
        .addObject("submitButtonText", EDIT_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(DocumentInstanceController.class)
                .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()))
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
            ReverseRouter.route(on(DocumentInstanceController.class)
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

    return ReverseRouter.redirect(on(DocumentInstanceController.class)
        .getViewDocumentInstance(documentInstanceSectionDto.documentInstanceDto().id()));
  }
}

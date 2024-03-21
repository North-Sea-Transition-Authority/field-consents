package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

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
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/document-instances/section/{documentInstanceSectionId}")
@ActionEndPoint(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS)
public class ApplicationDocumentInstanceSectionController {

  static final String ADD_PAGE_TITLE = "Add section";
  static final String ADD_SUBMIT_BUTTON_TEXT = "Add";
  static final String EDIT_PAGE_TITLE = "Edit section";
  static final String EDIT_SUBMIT_BUTTON_TEXT = "Save";

  private final ApplicationDocumentInstanceSectionControllerHelperService
      applicationDocumentInstanceSectionControllerHelperService;
  private final DocumentInstanceSectionService documentInstanceSectionService;
  private final DocumentInstanceSectionFormValidator documentInstanceSectionFormValidator;
  private final DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService;
  private final DocumentMailMergeFieldControllerHelperService documentMailMergeFieldControllerHelperService;
  private final ApplicationService applicationService;

  ApplicationDocumentInstanceSectionController(
      ApplicationDocumentInstanceSectionControllerHelperService applicationDocumentInstanceSectionControllerHelperService,
      DocumentInstanceSectionService documentInstanceSectionService,
      DocumentInstanceSectionFormValidator documentInstanceSectionFormValidator,
      DocumentInstanceSectionControllerHelperService documentInstanceSectionControllerHelperService,
      DocumentMailMergeFieldControllerHelperService documentMailMergeFieldControllerHelperService,
      ApplicationService applicationService
  ) {
    this.applicationDocumentInstanceSectionControllerHelperService =
        applicationDocumentInstanceSectionControllerHelperService;
    this.documentInstanceSectionService = documentInstanceSectionService;
    this.documentInstanceSectionFormValidator = documentInstanceSectionFormValidator;
    this.documentInstanceSectionControllerHelperService = documentInstanceSectionControllerHelperService;
    this.documentMailMergeFieldControllerHelperService = documentMailMergeFieldControllerHelperService;
    this.applicationService = applicationService;
  }

  @GetMapping("/add-before")
  public ModelAndView getAddDocumentInstanceSectionBefore(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    return getAddDocumentInstanceSectionModelAndView(
        application,
        documentInstanceSectionDto,
        DocumentInstanceSectionForm.empty()
    );
  }

  @PostMapping("/add-before")
  public ModelAndView addDocumentInstanceSectionBefore(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    var parentId = documentInstanceSectionDto.parentId();
    var parentDocumentInstanceSectionDto = parentId != null
        ? documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId)
        : null;

    return addDocumentSection(
        application,
        documentInstanceSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        parentDocumentInstanceSectionDto,
        documentInstanceSectionDto.displayOrder()
    );
  }

  @GetMapping("/add-after")
  public ModelAndView getAddDocumentInstanceSectionAfter(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    return getAddDocumentInstanceSectionModelAndView(
        application,
        documentInstanceSectionDto,
        DocumentInstanceSectionForm.empty()
    );
  }

  @PostMapping("/add-after")
  public ModelAndView addDocumentInstanceSectionAfter(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    var parentId = documentInstanceSectionDto.parentId();
    var parentDocumentInstanceSectionDto = parentId != null
        ? documentInstanceSectionService.getDocumentInstanceSectionDtoOrThrow(parentId)
        : null;

    return addDocumentSection(
        application,
        documentInstanceSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        parentDocumentInstanceSectionDto,
        documentInstanceSectionDto.displayOrder() + 1
    );
  }

  @GetMapping("/add-subsection")
  public ModelAndView getAddDocumentInstanceSubsection(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    return getAddDocumentInstanceSectionModelAndView(
        application,
        documentInstanceSectionDto,
        DocumentInstanceSectionForm.empty()
    );
  }

  @PostMapping("/add-subsection")
  public ModelAndView addDocumentInstanceSubsection(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    return addDocumentSection(
        application,
        documentInstanceSectionDto,
        form,
        bindingResult,
        redirectAttributes,
        documentInstanceSectionDto,
        1
    );
  }

  private ModelAndView getAddDocumentInstanceSectionModelAndView(
      Application application,
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    return new ModelAndView("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection")
        .addObject("form", form)
        .addObject("pageTitle", ADD_PAGE_TITLE)
        .addObject(
            "mailMergeFieldViews",
            documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto)
        )
        .addObject("submitButtonText", ADD_SUBMIT_BUTTON_TEXT)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
                .getViewDocumentInstance(application.getId(), documentInstanceDto.id()))
        );
  }

  private ModelAndView addDocumentSection(
      Application application,
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
      return getAddDocumentInstanceSectionModelAndView(application, documentInstanceSectionDto, form);
    }

    documentInstanceSectionControllerHelperService.createDocumentInstanceSection(
        documentInstanceDto,
        parentDto,
        form,
        displayOrder
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section added");

    return ReverseRouter.redirect(on(ApplicationDocumentInstanceController.class)
        .getViewDocumentInstance(application.getId(), documentInstanceDto.id()));
  }

  @GetMapping("/edit")
  public ModelAndView getEditDocumentInstanceSection(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );
    var form = DocumentInstanceSectionForm.from(documentInstanceSectionDto);

    return getEditDocumentInstanceSectionModelAndView(application, documentInstanceSectionDto, form);
  }

  @PostMapping("/edit")
  public ModelAndView editDocumentInstanceSection(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId,
      @ModelAttribute("form") DocumentInstanceSectionForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();

    documentInstanceSectionFormValidator.validate(form, documentInstanceDto, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEditDocumentInstanceSectionModelAndView(application, documentInstanceSectionDto, form);
    }

    documentInstanceSectionControllerHelperService.editDocumentInstanceSection(documentInstanceSectionDto, form);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section saved");

    return ReverseRouter.redirect(on(ApplicationDocumentInstanceController.class)
        .getViewDocumentInstance(applicationId, documentInstanceDto.id()));
  }

  private ModelAndView getEditDocumentInstanceSectionModelAndView(
      Application application,
      DocumentInstanceSectionDto documentInstanceSectionDto,
      DocumentInstanceSectionForm form
  ) {
    var documentInstanceDto = documentInstanceSectionDto.documentInstanceDto();
    var documentTemplateDto = documentInstanceDto.documentTemplateDto();

    return new ModelAndView("fcs/application/caseprocessing/document/instance/addOrEditDocumentInstanceSection")
        .addObject("form", form)
        .addObject("pageTitle", EDIT_PAGE_TITLE)
        .addObject("submitButtonText", EDIT_SUBMIT_BUTTON_TEXT)
        .addObject(
            "mailMergeFieldViews",
            documentMailMergeFieldControllerHelperService.getApplicableDocumentMailMergeFieldViews(documentTemplateDto)
        )
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
                .getViewDocumentInstance(application.getId(), documentInstanceDto.id()))
        );
  }

  @GetMapping("/remove")
  public ModelAndView getRemoveDocumentInstanceSection(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    return new ModelAndView("fcs/document/removeDocumentSection")
        .addObject("documentSectionDto", documentInstanceSectionDto)
        .addObject("documentSectionDtoDescendants", documentInstanceSectionDto.descendants())
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
                .getViewDocumentInstance(applicationId, documentInstanceSectionDto.documentInstanceDto().id()))
        );
  }

  @PostMapping("/remove")
  public ModelAndView removeDocumentInstanceSection(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceSectionId,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceSectionDto =
        applicationDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionDtoForApplicationOrThrow(
            application,
            documentInstanceSectionId
        );

    documentInstanceSectionService.deleteDocumentInstanceSection(documentInstanceSectionDto);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Section removed");

    return ReverseRouter.redirect(on(ApplicationDocumentInstanceController.class)
        .getViewDocumentInstance(applicationId, documentInstanceSectionDto.documentInstanceDto().id()));
  }
}

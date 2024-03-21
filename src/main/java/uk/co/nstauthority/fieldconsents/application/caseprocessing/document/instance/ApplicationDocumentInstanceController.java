package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.net.HttpHeaders;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/document-instances/{documentInstanceId}")
public class ApplicationDocumentInstanceController {

  private final ApplicationDocumentInstanceService applicationDocumentInstanceService;
  private final ApplicationDocumentInstanceControllerHelperService applicationDocumentInstanceControllerHelperService;
  private final ApplicationDocumentInstanceSectionControllerHelperService
      applicationDocumentInstanceSectionControllerHelperService;
  private final DocumentInstanceService documentInstanceService;
  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;

  ApplicationDocumentInstanceController(
      ApplicationDocumentInstanceService applicationDocumentInstanceService,
      ApplicationDocumentInstanceControllerHelperService applicationDocumentInstanceControllerHelperService,
      ApplicationDocumentInstanceSectionControllerHelperService applicationDocumentInstanceSectionControllerHelperService,
      DocumentInstanceService documentInstanceService,
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService
  ) {
    this.applicationDocumentInstanceService = applicationDocumentInstanceService;
    this.applicationDocumentInstanceControllerHelperService = applicationDocumentInstanceControllerHelperService;
    this.applicationDocumentInstanceSectionControllerHelperService =
        applicationDocumentInstanceSectionControllerHelperService;
    this.documentInstanceService = documentInstanceService;
    this.applicationVersionService = applicationVersionService;
    this.applicationService = applicationService;
  }

  @GetMapping
  @ActionEndPoint(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS)
  public ModelAndView getViewDocumentInstance(@PathVariable Integer applicationId, @PathVariable UUID documentInstanceId) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceDto = applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
        application,
        documentInstanceId
    );

    var documentInstanceSectionsSummaryView = applicationDocumentInstanceSectionControllerHelperService
        .getDocumentInstanceSectionsSummaryView(application, documentInstanceDto, true);

    return new ModelAndView("fcs/application/caseprocessing/document/instance/viewDocumentInstance")
        .addObject("pageTitle", documentInstanceDto.documentTemplateDto().title())
        .addObject("documentInstanceSectionsSummaryView", documentInstanceSectionsSummaryView)
        .addObject(
            "previewUrl",
            ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
                .getPreviewDocumentInstance(applicationId, documentInstanceId))
        )
        .addObject(
            "reloadUrl",
            ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
                .getReloadDocumentInstance(applicationId, documentInstanceId))
        );
  }

  @GetMapping("/preview")
  @ActionEndPoint({ CaseProcessingActionItem.CONSENT_PREPARATION, CaseProcessingActionItem.CONSENT_ISSUING })
  public ResponseEntity<?> getPreviewDocumentInstance(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceDto = applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
        application,
        documentInstanceId
    );

    var byteArrayResource = applicationDocumentInstanceService.renderPdf(
        application,
        documentInstanceDto,
        PdfRenderingOptions.newBuilder().withPreviewWatermark(true).build()
    );
    var fileName = "PREVIEW %s.pdf".formatted(documentInstanceDto.title());

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(byteArrayResource.contentLength())
        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName))
        .body(byteArrayResource);
  }

  @GetMapping("/reload")
  @ActionEndPoint(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS)
  public ModelAndView getReloadDocumentInstance(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceId
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceDto = applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
        application,
        documentInstanceId
    );

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    return new ModelAndView("fcs/application/caseprocessing/document/instance/reloadDocumentInstance")
        .addObject("documentTitle", documentInstanceDto.documentTemplateDto().title())
        .addObject("applicationReference", applicationReference)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(ApplicationDocumentInstanceController.class)
                .getViewDocumentInstance(applicationId, documentInstanceId))
        );
  }

  @PostMapping("/reload")
  @ActionEndPoint(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS)
  public ModelAndView reloadDocumentInstance(
      @PathVariable Integer applicationId,
      @PathVariable UUID documentInstanceId,
      RedirectAttributes redirectAttributes
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var documentInstanceDto = applicationDocumentInstanceControllerHelperService.getDocumentInstanceDtoForApplicationOrThrow(
        application,
        documentInstanceId
    );

    documentInstanceService.reloadDocumentInstance(documentInstanceDto);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Document reloaded");

    return ReverseRouter.redirect(on(ApplicationDocumentInstanceController.class)
        .getViewDocumentInstance(applicationId, documentInstanceId));
  }
}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.fieldconsents.application.ApplicationFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;

@RestController
@RequestMapping("/applications/{applicationId}/consent-preparation/files")
public class ConsentPreparationFileController {

  private final ApplicationService applicationService;
  private final FileControllerHelperService fileControllerHelperService;

  ConsentPreparationFileController(
      ApplicationService applicationService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.applicationService = applicationService;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping("/{fileId}")
  @ActionEndPoint({ CaseProcessingActionItem.CONSENT_PREPARATION, CaseProcessingActionItem.CONSENT_ISSUING })
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.download(fileId, () -> getFileUsage(applicationId), userDetail);
  }

  @PostMapping("/delete/{fileId}")
  @ActionEndPoint(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS)
  public ResponseEntity<FileDeleteResponse> delete(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.delete(fileId, () -> getFileUsage(applicationId), userDetail);
  }

  private FieldConsentsFileUsage getFileUsage(Integer applicationId) {
    var application = applicationService.getApplicationById(applicationId);
    return ApplicationFileUsage.supportingConsentDocumentFrom(application);
  }

}

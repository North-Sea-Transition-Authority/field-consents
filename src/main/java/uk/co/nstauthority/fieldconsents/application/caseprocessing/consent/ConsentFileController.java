package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanViewConsent;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;

@RestController
@RequestMapping("/applications/{applicationId}/consent/files")
@UserCanViewConsent
public class ConsentFileController {

  private final ApplicationService applicationService;
  private final ConsentService consentService;
  private final FileControllerHelperService fileControllerHelperService;

  ConsentFileController(
      ApplicationService applicationService,
      ConsentService consentService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.applicationService = applicationService;
    this.consentService = consentService;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping("/generated-consent-document/{fileId}")
  public ResponseEntity<InputStreamResource> downloadGeneratedConsentDocument(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var consent = consentService.getConsent(application);
    var generatedConsentDocumentConsentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    return fileControllerHelperService.download(fileId, () -> generatedConsentDocumentConsentFileUsage, userDetail);
  }

  @GetMapping("/supporting-consent-document/{fileId}")
  public ResponseEntity<InputStreamResource> downloadSupportingConsentDocument(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    var application = applicationService.getApplicationById(applicationId);
    var consent = consentService.getConsent(application);
    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    return fileControllerHelperService.download(fileId, () -> supportingConsentDocumentConsentFileUsage, userDetail);
  }
}

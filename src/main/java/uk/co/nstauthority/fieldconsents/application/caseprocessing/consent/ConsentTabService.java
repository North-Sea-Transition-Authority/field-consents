package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@Service
public class ConsentTabService {

  private final ConsentService consentService;
  private final FieldConsentsFileService fieldConsentsFileService;
  private final EnergyPortalUserService energyPortalUserService;

  ConsentTabService(
      ConsentService consentService,
      FieldConsentsFileService fieldConsentsFileService,
      EnergyPortalUserService energyPortalUserService
  ) {
    this.consentService = consentService;
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void addConsentTabContentToModelAndView(Application application, ModelAndView modelAndView) {
    consentService.findConsent(application).ifPresent(consent -> {
      var issuedByUser =
          ServiceUserDetail.from(energyPortalUserService.getByWuaId(WebUserAccountId.from(consent.getIssuedByWuaId())));

      var generatedConsentDocumentSummaryFileViews = getGeneratedConsentDocumentSummaryFileViews(application, consent);
      var supportingConsentDocumentSummaryFileViews = getSupportingConsentDocumentSummaryFileViews(application, consent);
      var summaryFileViews =
          Stream.concat(generatedConsentDocumentSummaryFileViews, supportingConsentDocumentSummaryFileViews).toList();

      var consentTabConsentSummaryView = ConsentTabConsentSummaryView.from(consent, issuedByUser, summaryFileViews);

      modelAndView.addObject("consentTabConsentSummaryView", consentTabConsentSummaryView);
    });
  }

  Stream<SummaryFileView> getGeneratedConsentDocumentSummaryFileViews(Application application, Consent consent) {
    var generatedConsentDocumentConsentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    return fieldConsentsFileService.getUploadedFiles(generatedConsentDocumentConsentFileUsage)
        .stream()
        .map(uploadedFile ->
            SummaryFileView.from(
                uploadedFile,
                ReverseRouter.route(on(ConsentFileController.class)
                    .downloadGeneratedConsentDocument(application.getId(), uploadedFile.getId(), null))
            )
        );
  }

  Stream<SummaryFileView> getSupportingConsentDocumentSummaryFileViews(Application application, Consent consent) {
    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    return fieldConsentsFileService.getUploadedFiles(supportingConsentDocumentConsentFileUsage)
        .stream()
        .map(uploadedFile ->
            SummaryFileView.from(
                uploadedFile,
                ReverseRouter.route(on(ConsentFileController.class)
                    .downloadSupportingConsentDocument(application.getId(), uploadedFile.getId(), null))
            )
        );
  }
}

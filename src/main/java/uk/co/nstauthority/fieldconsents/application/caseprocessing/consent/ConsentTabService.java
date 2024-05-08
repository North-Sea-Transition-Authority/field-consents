package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnersView;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
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
  private final ConsentDataService consentDataService;
  private final ConsentFigureUnitService consentFigureUnitService;
  private final ConsentLengthService consentLengthService;
  private final ApplicationAssetService applicationAssetService;
  private final ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  ConsentTabService(
      ConsentService consentService,
      FieldConsentsFileService fieldConsentsFileService,
      EnergyPortalUserService energyPortalUserService,
      ConsentDataService consentDataService,
      ConsentFigureUnitService consentFigureUnitService,
      ConsentLengthService consentLengthService,
      ApplicationAssetService applicationAssetService,
      ConsentFieldEquityPartnerService consentFieldEquityPartnerService
  ) {
    this.consentService = consentService;
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.energyPortalUserService = energyPortalUserService;
    this.consentDataService = consentDataService;
    this.consentFigureUnitService = consentFigureUnitService;
    this.consentLengthService = consentLengthService;
    this.applicationAssetService = applicationAssetService;
    this.consentFieldEquityPartnerService = consentFieldEquityPartnerService;
  }

  public void addConsentTabContentToModelAndView(
      ApplicationVersion applicationVersion,
      ModelAndView modelAndView
  ) {
    var application = applicationVersion.getApplication();
    consentService.findConsent(application).ifPresent(consent -> {
      var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();
      var issuedByUser =
          ServiceUserDetail.from(energyPortalUserService.getByWuaId(WebUserAccountId.from(consent.getIssuedByWuaId())));

      var consentData = consentDataService.getConsentData(application);
      var consentDataView = consentDataService.getConsentDataView(application, consentData, consentLengthType);
      var consentFigureUnitView = consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType);

      ConsentFieldEquityPartnersView consentFieldEquityPartnersView = null;
      if (applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
        consentFieldEquityPartnersView = consentFieldEquityPartnerService.getConsentFieldEquityPartnersView(consent);
      }

      var generatedConsentDocumentSummaryFileViews = getGeneratedConsentDocumentSummaryFileViews(application, consent);
      var supportingConsentDocumentSummaryFileViews = getSupportingConsentDocumentSummaryFileViews(application, consent);
      var summaryFileViews =
          Stream.concat(generatedConsentDocumentSummaryFileViews, supportingConsentDocumentSummaryFileViews).toList();

      var consentTabConsentSummaryView = ConsentTabConsentSummaryView.from(
          application.getType(),
          consentLengthType,
          issuedByUser,
          consent.getIssuedInstant(),
          consentDataView,
          consentFigureUnitView,
          consentFieldEquityPartnersView,
          summaryFileViews
      );

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

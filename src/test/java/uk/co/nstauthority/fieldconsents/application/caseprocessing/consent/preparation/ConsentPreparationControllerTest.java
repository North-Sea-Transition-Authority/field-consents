package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceSummaryViewTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnersViewTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@ContextConfiguration(classes = ConsentPreparationController.class)
class ConsentPreparationControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ConsentDataService consentDataService;

  @MockBean
  private ConsentFigureUnitService consentFigureUnitService;

  @MockBean
  private ConsentLengthService consentLengthService;

  @MockBean
  private ConsentPreparationDocumentService consentDocumentService;

  @MockBean
  private FieldEquityPartnerService fieldEquityPartnerService;

  @MockBean
  private ApplicationAssetService applicationAssetService;

  private Application application;
  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
  }

  @SecurityTest
  void viewConsentPreparationPage_notSignedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class)
        .viewConsentPreparationPage(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void viewConsentPreparationPage_userDoesNotHaveConsentPreparationCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user)).thenReturn(Collections.emptyList());
    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class)
            .viewConsentPreparationPage(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void viewConsentPreparationPage_consentDataDoesNotExistAndUserHasEditConsentDataCaseProcessingActionItem() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.CONSENT_PREPARATION, CaseProcessingActionItem.EDIT_CONSENT_DATA));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class)
            .viewConsentPreparationPage(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsentDataController.class).editConsentData(APPLICATION_ID))));
  }

  @SecurityTest
  void viewConsentPreparationPage_consentDataDoesNotExistAndUserDoesNotHaveEditConsentDataCaseProcessingActionItem() throws Exception {
    var consentPreparationGroupActionViewList =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.CAM_ASSIGN_OWNERSHIP, applicationVersion));

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.CONSENT_PREPARATION));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.empty());

    when(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, CaseProcessingActionGroup.CONSENT_PREPARATION))
        .thenReturn(consentPreparationGroupActionViewList);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class)
            .viewConsentPreparationPage(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/consentPreparation"))
        .andExpect(model().attribute("pageTitle", "Consent preparation"))
        .andExpect(model().attribute("applicationType", application.getType()))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(model().attribute("consentPreparationGroupActionViewList", consentPreparationGroupActionViewList));
  }

  @SecurityTest
  void viewConsentPreparationPage_consentDataExists() throws Exception {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentPreparationGroupActionViewList =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.CAM_ASSIGN_OWNERSHIP, applicationVersion));

    var consentLengthType = ConsentLengthType.SHORT_TERM;
    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var consentDataView = ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(ProductionUnit.KSCM_PER_DAY);
    var consentPreparationConsentDataCardGroupActionViewList =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.EDIT_CONSENT_DATA, applicationVersion));

    var documentInstanceSummaryView = DocumentInstanceSummaryViewTestUtil.newBuilder().build();
    var consentDocumentsSummaryCard = SummaryCard.filesSummaryCardWithHeading(
        "Consent documents",
        List.of(SummaryFileView.previewSummaryFrom(application, documentInstanceSummaryView))
    );
    var consentPreparationConsentDocumentsCardGroupActionViewList =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS, applicationVersion));

    var applicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();
    var fieldEquityPartnerView = FieldEquityPartnersViewTestUtil.newBuilder().build();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(CaseProcessingActionItem.CONSENT_PREPARATION));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(consentDataService.findConsentData(application)).thenReturn(Optional.of(consentData));

    when(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, CaseProcessingActionGroup.CONSENT_PREPARATION))
        .thenReturn(consentPreparationGroupActionViewList);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    when(consentDataService.getConsentDataView(application, consentData, consentLengthType)).thenReturn(consentDataView);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);
    when(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, CaseProcessingActionGroup.CONSENT_PREPARATION_CONSENT_DATA_CARD))
        .thenReturn(consentPreparationConsentDataCardGroupActionViewList);

    when(consentDocumentService.getConsentDocumentsSummaryCard(application)).thenReturn(consentDocumentsSummaryCard);
    when(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, CaseProcessingActionGroup.CONSENT_PREPARATION_CONSENT_DOCUMENTS_CARD))
        .thenReturn(consentPreparationConsentDocumentsCardGroupActionViewList);

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);
    when(fieldEquityPartnerService.getFieldEquityPartnersView(applicationVersion)).thenReturn(fieldEquityPartnerView);

    mockMvc.perform(get(ReverseRouter.route(on(ConsentPreparationController.class)
            .viewConsentPreparationPage(APPLICATION_ID, null)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/consent/consentPreparation"))
        .andExpect(model().attribute("pageTitle", "Consent preparation"))
        .andExpect(model().attribute("applicationType", application.getType()))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(APPLICATION_ID, null, null))))
        .andExpect(model().attribute("consentPreparationGroupActionViewList", consentPreparationGroupActionViewList))
        .andExpect(model().attribute("consentLengthType", consentLengthType))
        .andExpect(model().attribute("consentDataView", consentDataView))
        .andExpect(model().attribute("consentFigureUnitView", consentFigureUnitView))
        .andExpect(model().attribute("consentPreparationConsentDataCardGroupActionViewList", consentPreparationConsentDataCardGroupActionViewList))
        .andExpect(model().attribute("consentDocumentsSummaryCard", consentDocumentsSummaryCard))
        .andExpect(model().attribute("consentPreparationConsentDocumentsCardGroupActionViewList", consentPreparationConsentDocumentsCardGroupActionViewList))
        .andExpect(model().attribute("fieldEquityPartnersView", fieldEquityPartnerView));
  }
}

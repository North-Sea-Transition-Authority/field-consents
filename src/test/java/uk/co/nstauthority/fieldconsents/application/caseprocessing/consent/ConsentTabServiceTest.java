package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivum.fileuploadlibrary.core.UploadedFileTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnersView;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FormattedFieldEquityPartner;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@ExtendWith(MockitoExtension.class)
class ConsentTabServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ConsentService consentService;

  @Mock
  private ConsentDataService consentDataService;

  @Mock
  private ConsentFigureUnitService consentFigureUnitService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ConsentTabService consentTabService;

  @BeforeEach
  void beforeEach() {
    consentTabService = spy(new ConsentTabService(
        applicationService,
        applicationVersionService,
        applicationAssetService,
        consentService,
        consentDataService,
        consentFigureUnitService,
        consentLengthService,
        consentFieldEquityPartnerService,
        fieldConsentsFileService,
        energyPortalUserService,
        clock
    ));
  }

  @Test
  void addConsentTabContentToModelAndView_consentDoesNotExist() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();
    var modelAndView = new ModelAndView();

    when(consentService.findConsent(application)).thenReturn(Optional.empty());

    consentTabService.addConsentTabContentToModelAndView(applicationVersion, modelAndView);

    assertThat(modelAndView.getModel()).doesNotContainKey("consentTabConsentSummaryView");
  }

  @Test
  void addConsentTabContentToModelAndView_consentExists_primaryAssetIsField_consentIsNotSuperseded() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentLengthType = ConsentLengthType.ANNUAL;
    var modelAndView = new ModelAndView();

    var consent = ConsentTestUtil.newBuilder()
        .withSupersededByConsent(null)
        .build();
    var energyPortalUserDto = mock(EnergyPortalUserDto.class);

    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentDataView = ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(
        ProductionUnit.KSCM_PER_DAY);
    var consentFieldEquityPartnersView = new ConsentFieldEquityPartnersView(
          List.of(new FormattedFieldEquityPartner("ORG1", "12345678")
              , new FormattedFieldEquityPartner("ORG2", "87654321")
              , new FormattedFieldEquityPartner("ORG3", null)
          )
      );

    var generatedConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var generatedConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    var supportingConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var supportingConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    when(consentService.findConsent(application))
        .thenReturn(Optional.of(consent));
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);
    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(consent.getIssuedByWuaId())))
        .thenReturn(energyPortalUserDto);
    when(consentDataService.getConsentData(application))
        .thenReturn(consentData);
    when(consentDataService.getConsentDataView(application, consentData, consentLengthType))
        .thenReturn(consentDataView);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersView(consent))
        .thenReturn(consentFieldEquityPartnersView);

    doReturn(Stream.of(generatedConsentDocumentSummaryFileView1, generatedConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getGeneratedConsentDocumentSummaryFileViews(application, consent);

    doReturn(Stream.of(supportingConsentDocumentSummaryFileView1, supportingConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getSupportingConsentDocumentSummaryFileViews(application, consent);

    consentTabService.addConsentTabContentToModelAndView(applicationVersion, modelAndView);

    assertThat(modelAndView.getModel()).containsEntry(
        "consentTabConsentSummaryView",
        ConsentTabConsentSummaryView.from(
            application.getType(),
            consentLengthType,
            ServiceUserDetail.from(energyPortalUserDto),
            consent.getIssuedInstant(),
            ConsentStatus.ACTIVE,
            null,
            consentDataView,
            consentFigureUnitView,
            consentFieldEquityPartnersView,
            List.of(
                generatedConsentDocumentSummaryFileView1,
                generatedConsentDocumentSummaryFileView2,
                supportingConsentDocumentSummaryFileView1,
                supportingConsentDocumentSummaryFileView2
            )
        )
    );
  }

  @Test
  void addConsentTabContentToModelAndView_consentExists_primaryAssetIsField_consentIsSuperseded() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentLengthType = ConsentLengthType.ANNUAL;
    var modelAndView = new ModelAndView();

    var consentSupersededByConsent = ConsentTestUtil.newBuilder()
        .withId(2)
        .build();
    var consentSupersededByConsentLatestApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var consentSupersededByApplicationReference = "Test/application/reference";

    var consent = ConsentTestUtil.newBuilder()
        .withId(1)
        .withSupersededByConsent(consentSupersededByConsent)
        .build();
    var energyPortalUserDto = mock(EnergyPortalUserDto.class);

    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentDataView = ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(
        ProductionUnit.KSCM_PER_DAY);
    var consentFieldEquityPartnersView = new ConsentFieldEquityPartnersView(
        List.of(new FormattedFieldEquityPartner("ORG1", "12345678")
            , new FormattedFieldEquityPartner("ORG2", "87654321")
            , new FormattedFieldEquityPartner("ORG3", null)
        )
    );

    var generatedConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var generatedConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    var supportingConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var supportingConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    when(consentService.findConsent(application))
        .thenReturn(Optional.of(consent));
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);
    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(consent.getIssuedByWuaId())))
        .thenReturn(energyPortalUserDto);
    when(consentDataService.getConsentData(application))
        .thenReturn(consentData);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(consentSupersededByConsent.getApplication().getId()))
        .thenReturn(consentSupersededByConsentLatestApplicationVersion);
    when(applicationService.generateApplicationReference(consentSupersededByConsentLatestApplicationVersion))
        .thenReturn(consentSupersededByApplicationReference);
    when(consentDataService.getConsentDataView(application, consentData, consentLengthType))
        .thenReturn(consentDataView);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersView(consent))
        .thenReturn(consentFieldEquityPartnersView);

    doReturn(Stream.of(generatedConsentDocumentSummaryFileView1, generatedConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getGeneratedConsentDocumentSummaryFileViews(application, consent);

    doReturn(Stream.of(supportingConsentDocumentSummaryFileView1, supportingConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getSupportingConsentDocumentSummaryFileViews(application, consent);

    consentTabService.addConsentTabContentToModelAndView(applicationVersion, modelAndView);

    assertThat(modelAndView.getModel()).containsEntry(
        "consentTabConsentSummaryView",
        ConsentTabConsentSummaryView.from(
            application.getType(),
            consentLengthType,
            ServiceUserDetail.from(energyPortalUserDto),
            consent.getIssuedInstant(),
            ConsentStatus.SUPERSEDED,
            consentSupersededByApplicationReference,
            consentDataView,
            consentFigureUnitView,
            consentFieldEquityPartnersView,
            List.of(
                generatedConsentDocumentSummaryFileView1,
                generatedConsentDocumentSummaryFileView2,
                supportingConsentDocumentSummaryFileView1,
                supportingConsentDocumentSummaryFileView2
            )
        )
    );
  }

  @Test
  void addConsentTabContentToModelAndView_consentExists_primaryAssetIsTerminal() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
    var consentLengthType = ConsentLengthType.ANNUAL;
    var modelAndView = new ModelAndView();

    var consent = ConsentTestUtil.newBuilder()
        .withSupersededByConsent(null)
        .build();
    var energyPortalUserDto = mock(EnergyPortalUserDto.class);

    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentDataView = ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
    var consentFigureUnitView = ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(
        ProductionUnit.KSCM_PER_DAY);

    var generatedConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var generatedConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    var supportingConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var supportingConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    when(consentService.findConsent(application))
        .thenReturn(Optional.of(consent));
    when(consentLengthService.getConsentLengthDetails(applicationVersion))
        .thenReturn(consentLengthDetails);
    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(consent.getIssuedByWuaId())))
        .thenReturn(energyPortalUserDto);
    when(consentDataService.getConsentData(application))
        .thenReturn(consentData);
    when(consentDataService.getConsentDataView(application, consentData, consentLengthType))
        .thenReturn(consentDataView);
    when(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .thenReturn(consentFigureUnitView);
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.terminalAsset1);

    doReturn(Stream.of(generatedConsentDocumentSummaryFileView1, generatedConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getGeneratedConsentDocumentSummaryFileViews(application, consent);

    doReturn(Stream.of(supportingConsentDocumentSummaryFileView1, supportingConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getSupportingConsentDocumentSummaryFileViews(application, consent);

    consentTabService.addConsentTabContentToModelAndView(applicationVersion, modelAndView);

    assertThat(modelAndView.getModel()).containsEntry(
        "consentTabConsentSummaryView",
        ConsentTabConsentSummaryView.from(
            application.getType(),
            consentLengthType,
            ServiceUserDetail.from(energyPortalUserDto),
            consent.getIssuedInstant(),
            ConsentStatus.ACTIVE,
            null,
            consentDataView,
            consentFigureUnitView,
            null,
            List.of(
                generatedConsentDocumentSummaryFileView1,
                generatedConsentDocumentSummaryFileView2,
                supportingConsentDocumentSummaryFileView1,
                supportingConsentDocumentSummaryFileView2
            )
        )
    );
  }

  @Test
  void getGeneratedConsentDocumentSummaryFileViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consent = ConsentTestUtil.newBuilder().build();

    var generatedConsentDocumentConsentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    var uploadedFile1 = UploadedFileTestUtil.newBuilder().build();
    var uploadedFile2 = UploadedFileTestUtil.newBuilder().build();

    when(fieldConsentsFileService.getUploadedFiles(generatedConsentDocumentConsentFileUsage))
        .thenReturn(List.of(uploadedFile1, uploadedFile2));

    assertThat(consentTabService.getGeneratedConsentDocumentSummaryFileViews(application, consent)).containsExactly(
        SummaryFileView.from(
            uploadedFile1,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadGeneratedConsentDocument(application.getId(), uploadedFile1.getId(), null))
        ),
        SummaryFileView.from(
            uploadedFile2,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadGeneratedConsentDocument(application.getId(), uploadedFile2.getId(), null))
        )
    );
  }

  @Test
  void getSupportingConsentDocumentSummaryFileViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consent = ConsentTestUtil.newBuilder().build();

    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    var uploadedFile1 = UploadedFileTestUtil.newBuilder().withName("a").build();
    var uploadedFile2 = UploadedFileTestUtil.newBuilder().withName("B").build();

    when(fieldConsentsFileService.getUploadedFiles(supportingConsentDocumentConsentFileUsage))
        .thenReturn(List.of(uploadedFile2, uploadedFile1));

    assertThat(consentTabService.getSupportingConsentDocumentSummaryFileViews(application, consent)).containsExactly(
        SummaryFileView.from(
            uploadedFile1,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadSupportingConsentDocument(application.getId(), uploadedFile1.getId(), null))
        ),
        SummaryFileView.from(
            uploadedFile2,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadSupportingConsentDocument(application.getId(), uploadedFile2.getId(), null))
        )
    );
  }
}

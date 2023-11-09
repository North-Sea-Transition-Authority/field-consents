package uk.co.nstauthority.fieldconsents.application.payment;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.fivium.digitalpaymentslibrary.payment.CreateCardPaymentResult;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentService;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.consentrevision.ConsentRevisionType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fee.FeeLineMnemonic;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class ApplicationPaymentServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Mock
  private PaymentService paymentService;

  @Mock
  private FeePeriodService feePeriodService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  @Spy
  private ApplicationPaymentService applicationPaymentService;

  @Test
  void getPaymentAmountPence() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    var consentLengthDetails = new ConsentLengthDetails();
    var consentLength = ConsentLengthType.SHORT_TERM;
    consentLengthDetails.setConsentLength(consentLength);

    var mnemonic = FeeLineMnemonic.from(
        primaryAsset.getAssetType(),
        application.getType(),
        consentLength,
        ConsentRevisionType.from(application)
    );

    var currentCostPence = 93000;

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(feePeriodService.getCurrentCost(mnemonic.mnemonic())).thenReturn(currentCostPence);

    assertThat(applicationPaymentService.getPaymentAmountPence(applicationVersion)).isEqualTo(currentCostPence);
  }

  @Test
  void createPayment() {
    var applicationVersion = new ApplicationVersion();
    var user = ServiceUserDetailTestUtil.Builder().build();

    var paymentItemReference = "testPaymentItemReference";
    var paymentAmountPence = 93000;
    var paymentDescription = "testPaymentDescription";
    Map<String, Object> paymentMetadata = Map.of("testPaymentMetadataKey", "testPaymentMetadataValue");
    Function<UUID, String> returnUrlFunction = paymentId -> "testReturnUrl";

    var createCardPaymentResult = CreateCardPaymentResult.success("testGovUkPayNextUrl");

    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);
    doReturn(paymentAmountPence).when(applicationPaymentService).getPaymentAmountPence(applicationVersion);
    doReturn(paymentDescription).when(applicationPaymentService).getPaymentDescription(applicationVersion);
    doReturn(paymentMetadata).when(applicationPaymentService).getPaymentMetadata(applicationVersion);

    when(paymentService.createCardPayment(
        paymentItemReference,
        ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE,
        paymentAmountPence,
        paymentDescription,
        paymentMetadata,
        returnUrlFunction,
        user.wuaId().toString()
    )).thenReturn(createCardPaymentResult);

    assertThat(applicationPaymentService.createPayment(applicationVersion, user, returnUrlFunction))
        .isEqualTo(createCardPaymentResult);
  }

  @Test
  void getPaymentItemReference() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    assertThat(applicationPaymentService.getPaymentItemReference(applicationVersion))
        .isEqualTo(applicationVersion.getId().toString());
  }

  @Test
  void getApplicationVersionFromPaymentItemReference() {
    var itemReference = "1";

    var applicationVersion = new ApplicationVersion();

    when(applicationVersionService.getApplicationVersionById(Integer.parseInt(itemReference)))
        .thenReturn(applicationVersion);

    assertThat(applicationPaymentService.getApplicationVersionFromPaymentItemReference(itemReference))
        .isEqualTo(applicationVersion);
  }

  @Test
  void getPaymentDescription_primaryAssetIsField() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var applicationReference = "testApplicationReference";

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    var paymentDescription = applicationPaymentService.getPaymentDescription(applicationVersion);

    assertThat(paymentDescription)
        .isEqualTo("New field short term consent application testApplicationReference submission");
  }

  @Test
  void getPaymentDescription_primaryAssetIsTerminal() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.TERMINAL);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var applicationReference = "testApplicationReference";

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    var paymentDescription = applicationPaymentService.getPaymentDescription(applicationVersion);

    assertThat(paymentDescription)
        .isEqualTo("New facility short term consent application testApplicationReference submission");
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_noSecondaryAssets() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryOperatorName = "testPrimaryOperatorName";
    var primaryOperatorOrganisationUnitId = 1;
    var primaryOperatorOrganisationUnitJson
        = new OrganisationUnitJson(primaryOperatorOrganisationUnitId, primaryOperatorName);

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(primaryAssetFieldId);
    primaryAsset.setAssetType(AssetType.FIELD);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var assets = List.of(primaryAsset);

    var fieldIds = List.of(primaryAssetFieldId);
    var fieldJsons = List.of(primaryAssetFieldJson);

    when(organisationUnitService.getOrganisationUnitById(primaryOperatorOrganisationUnitId, "Organisation lookup for payment metadata"))
        .thenReturn(primaryOperatorOrganisationUnitJson);
    when(applicationAssetService.findAssetsByApplicationVersion(applicationVersion)).thenReturn(assets);
    when(fieldService.findFieldsByIds(fieldIds, "Looking up field names for payment metadata"))
        .thenReturn(fieldJsons);

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Primary operator", primaryOperatorName),
        entry("Primary field", primaryAssetFieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_singleSecondaryFieldAsset() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryOperatorName = "testPrimaryOperatorName";
    var primaryOperatorOrganisationUnitId = 1;
    var primaryOperatorOrganisationUnitJson
        = new OrganisationUnitJson(primaryOperatorOrganisationUnitId, primaryOperatorName);

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(primaryAssetFieldId);
    primaryAsset.setAssetType(AssetType.FIELD);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAssetFieldId = 2;
    var secondaryAssetFieldName = "testSecondaryAssetFieldName";
    var secondaryAsset = new ApplicationAsset();
    secondaryAsset.setAssetId(secondaryAssetFieldId);
    secondaryAsset.setAssetType(AssetType.FIELD);
    secondaryAsset.setAssetRole(AssetRole.SECONDARY);
    var secondaryAssetFieldJson = new FieldJson(secondaryAssetFieldId, secondaryAssetFieldName, null, null, null);

    var assets = List.of(primaryAsset, secondaryAsset);

    var fieldIds = List.of(primaryAssetFieldId, secondaryAssetFieldId);
    var fieldJsons = List.of(primaryAssetFieldJson, secondaryAssetFieldJson);

    when(organisationUnitService.getOrganisationUnitById(primaryOperatorOrganisationUnitId, "Organisation lookup for payment metadata"))
        .thenReturn(primaryOperatorOrganisationUnitJson);
    when(applicationAssetService.findAssetsByApplicationVersion(applicationVersion)).thenReturn(assets);
    when(fieldService.findFieldsByIds(fieldIds, "Looking up field names for payment metadata"))
        .thenReturn(fieldJsons);

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Primary operator", primaryOperatorName),
        entry("Primary field", primaryAssetFieldName),
        entry("Additional field", secondaryAssetFieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_multipleSecondaryFieldAssets() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryOperatorName = "testPrimaryOperatorName";
    var primaryOperatorOrganisationUnitId = 1;
    var primaryOperatorOrganisationUnitJson
        = new OrganisationUnitJson(primaryOperatorOrganisationUnitId, primaryOperatorName);

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(primaryAssetFieldId);
    primaryAsset.setAssetType(AssetType.FIELD);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAsset1FieldId = 2;
    var secondaryAsset1FieldName = "testSecondaryAsset1FieldName";
    var secondaryAsset1 = new ApplicationAsset();
    secondaryAsset1.setAssetId(secondaryAsset1FieldId);
    secondaryAsset1.setAssetType(AssetType.FIELD);
    secondaryAsset1.setAssetRole(AssetRole.SECONDARY);
    var secondaryAsset1FieldJson = new FieldJson(secondaryAsset1FieldId, secondaryAsset1FieldName, null, null, null);

    var secondaryAsset2FieldId = 3;
    var secondaryAsset2FieldName = "testSecondaryAsset2FieldName";
    var secondaryAsset2 = new ApplicationAsset();
    secondaryAsset2.setAssetId(secondaryAsset2FieldId);
    secondaryAsset2.setAssetType(AssetType.FIELD);
    secondaryAsset2.setAssetRole(AssetRole.SECONDARY);
    var secondaryAsset2FieldJson = new FieldJson(secondaryAsset2FieldId, secondaryAsset2FieldName, null, null, null);

    var assets = List.of(primaryAsset, secondaryAsset1, secondaryAsset2);

    var fieldIds = List.of(primaryAssetFieldId, secondaryAsset1FieldId, secondaryAsset2FieldId);
    var fieldJsons = List.of(primaryAssetFieldJson, secondaryAsset1FieldJson, secondaryAsset2FieldJson);

    when(organisationUnitService.getOrganisationUnitById(primaryOperatorOrganisationUnitId, "Organisation lookup for payment metadata"))
        .thenReturn(primaryOperatorOrganisationUnitJson);
    when(applicationAssetService.findAssetsByApplicationVersion(applicationVersion)).thenReturn(assets);
    when(fieldService.findFieldsByIds(fieldIds, "Looking up field names for payment metadata"))
        .thenReturn(fieldJsons);

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Primary operator", primaryOperatorName),
        entry("Primary field", primaryAssetFieldName),
        entry("Additional fields", secondaryAsset1FieldName + ", " + secondaryAsset2FieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_secondaryTerminalAssetNotIncluded() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryOperatorName = "testPrimaryOperatorName";
    var primaryOperatorOrganisationUnitId = 1;
    var primaryOperatorOrganisationUnitJson
        = new OrganisationUnitJson(primaryOperatorOrganisationUnitId, primaryOperatorName);

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(primaryAssetFieldId);
    primaryAsset.setAssetType(AssetType.FIELD);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAssetTerminalId = 2;
    var secondaryAsset = new ApplicationAsset();
    secondaryAsset.setAssetRole(AssetRole.SECONDARY);
    secondaryAsset.setAssetId(secondaryAssetTerminalId);
    secondaryAsset.setAssetType(AssetType.TERMINAL);

    var assets = List.of(primaryAsset, secondaryAsset);

    var fieldIds = List.of(primaryAssetFieldId);
    var fieldJsons = List.of(primaryAssetFieldJson);

    when(organisationUnitService.getOrganisationUnitById(primaryOperatorOrganisationUnitId, "Organisation lookup for payment metadata"))
        .thenReturn(primaryOperatorOrganisationUnitJson);
    when(applicationAssetService.findAssetsByApplicationVersion(applicationVersion)).thenReturn(assets);
    when(fieldService.findFieldsByIds(fieldIds, "Looking up field names for payment metadata"))
        .thenReturn(fieldJsons);

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Primary operator", primaryOperatorName),
        entry("Primary field", primaryAssetFieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsTerminal() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryOperatorName = "testPrimaryOperatorName";
    var primaryOperatorOrganisationUnitId = 1;
    var primaryOperatorOrganisationUnitJson
        = new OrganisationUnitJson(primaryOperatorOrganisationUnitId, primaryOperatorName);

    var primaryAssetTerminalId = 1;
    var primaryAssetTerminalName = "testPrimaryAssetTerminalName";
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetId(primaryAssetTerminalId);
    primaryAsset.setAssetType(AssetType.TERMINAL);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetTerminalJson = new TerminalJson(primaryAssetTerminalId, primaryAssetTerminalName, null);

    var assets = List.of(primaryAsset);

    when(organisationUnitService.getOrganisationUnitById(primaryOperatorOrganisationUnitId, "Organisation lookup for payment metadata"))
        .thenReturn(primaryOperatorOrganisationUnitJson);
    when(applicationAssetService.findAssetsByApplicationVersion(applicationVersion)).thenReturn(assets);
    when(terminalService.getTerminal(primaryAssetTerminalId, "Looking up terminal name for payment metadata"))
        .thenReturn(primaryAssetTerminalJson);

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Primary operator", primaryOperatorName),
        entry("Facility", primaryAssetTerminalName)
    );
  }

  @Test
  void isPaymentForApplicationVersion_itemReferenceDoesNotEqualApplicationVersionItemReference() {
    var paymentId = UUID.randomUUID();
    var applicationVersion = new ApplicationVersion();

    var paymentDto = mock(PaymentDto.class);

    when(paymentDto.itemReference()).thenReturn("otherPaymentItemReference");
    when(paymentService.getPaymentDtoOrThrow(paymentId)).thenReturn(paymentDto);

    doReturn("testPaymentItemReference").when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    assertThat(applicationPaymentService.isPaymentForApplicationVersion(paymentId, applicationVersion)).isFalse();
  }

  @Test
  void isPaymentForApplicationVersion_itemTypeDoesNotEqualApplicationVersionItemType() {
    var paymentId = UUID.randomUUID();
    var applicationVersion = new ApplicationVersion();

    var paymentDto = mock(PaymentDto.class);
    var paymentItemReference = "testPaymentItemReference";

    when(paymentDto.itemReference()).thenReturn(paymentItemReference);
    when(paymentDto.itemType()).thenReturn("otherPaymentItemType");

    when(paymentService.getPaymentDtoOrThrow(paymentId)).thenReturn(paymentDto);
    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    assertThat(applicationPaymentService.isPaymentForApplicationVersion(paymentId, applicationVersion)).isFalse();
  }

  @Test
  void isPaymentForApplicationVersion() {
    var paymentId = UUID.randomUUID();
    var applicationVersion = new ApplicationVersion();

    var paymentDto = mock(PaymentDto.class);
    var paymentItemReference = "testPaymentItemReference";

    when(paymentDto.itemReference()).thenReturn(paymentItemReference);
    when(paymentDto.itemType()).thenReturn(ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE);

    when(paymentService.getPaymentDtoOrThrow(paymentId)).thenReturn(paymentDto);
    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    assertThat(applicationPaymentService.isPaymentForApplicationVersion(paymentId, applicationVersion)).isTrue();
  }

  @Test
  void getPaymentDtos() {
    var applicationVersion = new ApplicationVersion();

    var paymentItemReference = "testPaymentItemReference";

    var paymentDtos = List.of(mock(PaymentDto.class), mock(PaymentDto.class));

    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    when(paymentService.getPaymentDtos(paymentItemReference, ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE))
        .thenReturn(paymentDtos);

    assertThat(applicationPaymentService.getPaymentDtos(applicationVersion)).isEqualTo(paymentDtos);
  }

  @Test
  void getAndRefreshPaymentDtos() {
    var applicationVersion = new ApplicationVersion();

    var paymentItemReference = "testPaymentItemReference";

    var paymentDtos = List.of(mock(PaymentDto.class), mock(PaymentDto.class));

    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    when(paymentService.getAndRefreshPaymentDtos(paymentItemReference, ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE))
        .thenReturn(paymentDtos);

    assertThat(applicationPaymentService.getAndRefreshPaymentDtos(applicationVersion)).isEqualTo(paymentDtos);
  }

  @Test
  void cancelInProgressPayments() {
    var paymentDto1 = mock(PaymentDto.class);
    var paymentDto2 = mock(PaymentDto.class);
    var paymentDtos = List.of(paymentDto1, paymentDto2);

    when(paymentDto1.status()).thenReturn(PaymentStatus.FAILED);
    when(paymentDto2.status()).thenReturn(PaymentStatus.IN_PROGRESS);

    applicationPaymentService.cancelInProgressPayments(paymentDtos);

    verify(paymentService).cancelPayment(paymentDto2);
  }

  @Test
  void onPaymentReconcileSuccessEvent_itemTypeNotApplicationVersionPaymentItemType() {
    var paymentDto = mock(PaymentDto.class);

    when(paymentDto.itemType()).thenReturn("testItemType");

    assertThatThrownBy(() -> applicationPaymentService.onPaymentReconcileSuccessEvent(paymentDto))
        .isInstanceOf(IllegalStateException.class);

    verify(applicationService, never()).submitApplication(any(), any());
  }

  @Test
  void onPaymentReconcileSuccessEvent_applicationStatusNotAwaitingPayment() {
    var paymentDto = mock(PaymentDto.class);

    var itemReference = "testItemReference";
    var applicationVersion =
        ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(paymentDto.itemType()).thenReturn(ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE);
    when(paymentDto.itemReference()).thenReturn(itemReference);

    doReturn(applicationVersion)
        .when(applicationPaymentService)
        .getApplicationVersionFromPaymentItemReference(itemReference);

    assertThatThrownBy(() -> applicationPaymentService.onPaymentReconcileSuccessEvent(paymentDto))
        .isInstanceOf(IllegalStateException.class);

    verify(applicationService, never()).submitApplication(any(), any());
  }

  @Test
  void onPaymentReconcileSuccessEvent() {
    var paymentDto = mock(PaymentDto.class);

    var itemReference = "testItemReference";
    var createdByUserId = "1";
    var applicationVersion =
        ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var energyPortalUserDto = mock(EnergyPortalUserDto.class);

    when(paymentDto.itemType()).thenReturn(ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE);
    when(paymentDto.itemReference()).thenReturn(itemReference);
    when(paymentDto.createdByUserId()).thenReturn(createdByUserId);

    doReturn(applicationVersion)
        .when(applicationPaymentService)
        .getApplicationVersionFromPaymentItemReference(itemReference);
    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(createdByUserId))).thenReturn(energyPortalUserDto);
    when(energyPortalUserDto.webUserAccountId()).thenReturn(Long.valueOf(createdByUserId));

    applicationPaymentService.onPaymentReconcileSuccessEvent(paymentDto);

    var userCaptor = ArgumentCaptor.forClass(ServiceUserDetail.class);

    verify(applicationService).submitApplication(eq(applicationVersion), userCaptor.capture());

    assertThat(userCaptor.getValue().wuaId()).isEqualTo(Long.valueOf(createdByUserId));
  }
}

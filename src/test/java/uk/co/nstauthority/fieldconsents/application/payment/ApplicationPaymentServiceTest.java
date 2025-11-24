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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.revision.ApplicationRevisionType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.fee.FeeLineMnemonic;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class ApplicationPaymentServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationSubmissionService applicationSubmissionService;

  @Mock
  private ApplicationContextService applicationContextService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private PaymentService paymentService;

  @Mock
  private FeePeriodService feePeriodService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private OrganisationUnitService organisationUnitService;

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
        ApplicationRevisionType.from(application)
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
    var paymentMetadata = Map.of("testPaymentMetadataKey", "testPaymentMetadataValue");
    Function<UUID, String> returnUrlFunction = paymentId -> "testReturnUrl";

    var createCardPaymentResult = mock(CreateCardPaymentResult.class);

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
  void getPaymentDescription_primaryAssetIsField_revisionTypeIsNewConsent() {
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
  void getPaymentDescription_primaryAssetIsTerminal_revisionTypeIsNewConsent() {
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
  void getPaymentDescription_primaryAssetIsField_revisionTypeIsRevision() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersion.getApplication().setVariationNo(1);

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
        .isEqualTo("Revised field short term consent application testApplicationReference submission");
  }

  @Test
  void getPaymentDescription_primaryAssetIsTerminal_revisionTypeIsRevision() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersion.getApplication().setVariationNo(1);

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
        .isEqualTo("Revised facility short term consent application testApplicationReference submission");
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_noSecondaryAssets() {
    var applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "testApplicationPaymentReference";

    var primaryOperatorName = "testPrimaryOperatorName";
    var registeredNumber = "12345678";

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    when(applicationService.generateApplicationShortReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(primaryAssetFieldJson)
        .withPrimaryOperator(primaryOperatorName)
        .build());
    when(organisationUnitService.getOrganisationUnitRegisteredNumberOrForeignRegisteredNumber(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit registered number lookup for payment metadata"
    )).thenReturn(Optional.of(registeredNumber));

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Application reference", applicationReference),
        entry("Primary operator", primaryOperatorName),
        entry("Primary operator reg number", registeredNumber),
        entry("Primary field", primaryAssetFieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_singleSecondaryFieldAsset() {
    var applicationVersion  = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "testApplicationPaymentReference";

    var primaryOperatorName = "testPrimaryOperatorName";
    var registeredNumber = "12345678";

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAssetFieldName = "testSecondaryAssetFieldName";

    when(applicationService.generateApplicationShortReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(primaryAssetFieldJson)
        .withPrimaryOperator(primaryOperatorName)
        .withAdditionalFields(Collections.singleton(secondaryAssetFieldName))
        .build());
    when(organisationUnitService.getOrganisationUnitRegisteredNumberOrForeignRegisteredNumber(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit registered number lookup for payment metadata"
    )).thenReturn(Optional.of(registeredNumber));

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Application reference", applicationReference),
        entry("Primary operator", primaryOperatorName),
        entry("Primary operator reg number", registeredNumber),
        entry("Primary field", primaryAssetFieldName),
        entry("Additional field", secondaryAssetFieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_multipleSecondaryFieldAssets() {
    var applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "testApplicationPaymentReference";

    var primaryOperatorName = "testPrimaryOperatorName";
    var registeredNumber = "12345678";

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAsset1FieldName = "testSecondaryAsset1FieldName";
    var secondaryAsset2FieldName = "testSecondaryAsset2FieldName";

    when(applicationService.generateApplicationShortReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(primaryAssetFieldJson)
        .withPrimaryOperator(primaryOperatorName)
        .withAdditionalFields(Set.of(secondaryAsset1FieldName, secondaryAsset2FieldName))
        .build());
    when(organisationUnitService.getOrganisationUnitRegisteredNumberOrForeignRegisteredNumber(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit registered number lookup for payment metadata"
    )).thenReturn(Optional.of(registeredNumber));

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Application reference", applicationReference),
        entry("Primary operator", primaryOperatorName),
        entry("Primary operator reg number", registeredNumber),
        entry("Primary field", primaryAssetFieldName),
        entry("Additional fields", secondaryAsset1FieldName + ", " + secondaryAsset2FieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsField_secondaryTerminalAssetNotIncluded() {
    var applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "testApplicationPaymentReference";

    var primaryOperatorName = "testPrimaryOperatorName";
    var registeredNumber = "12345678";

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    when(applicationService.generateApplicationShortReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(primaryAssetFieldJson)
        .withPrimaryOperator(primaryOperatorName)
        .build());
    when(organisationUnitService.getOrganisationUnitRegisteredNumberOrForeignRegisteredNumber(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit registered number lookup for payment metadata"
    )).thenReturn(Optional.of(registeredNumber));

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Application reference", applicationReference),
        entry("Primary operator", primaryOperatorName),
        entry("Primary operator reg number", registeredNumber),
        entry("Primary field", primaryAssetFieldName)
    );
  }

  @Test
  void getPaymentMetadata_primaryAssetIsTerminal() {
    var applicationVersion  = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "testApplicationPaymentReference";

    var primaryOperatorName = "testPrimaryOperatorName";
    var registeredNumber = "12345678";

    var primaryAssetTerminalId = 1;
    var primaryAssetTerminalName = "testPrimaryAssetTerminalName";
    var primaryAssetTerminalJson = new TerminalJson(primaryAssetTerminalId, primaryAssetTerminalName, null);

    when(applicationService.generateApplicationShortReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(primaryAssetTerminalJson)
        .withPrimaryOperator(primaryOperatorName)
        .build());
    when(organisationUnitService.getOrganisationUnitRegisteredNumberOrForeignRegisteredNumber(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit registered number lookup for payment metadata"
    )).thenReturn(Optional.of(registeredNumber));

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Application reference", applicationReference),
        entry("Primary operator", primaryOperatorName),
        entry("Primary operator reg number", registeredNumber),
        entry("Facility", primaryAssetTerminalName)
    );
  }

  @Test
  void getPaymentMetadata_registeredNumberNotFound() {
    var applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var applicationReference = "testApplicationPaymentReference";

    var primaryOperatorName = "testPrimaryOperatorName";

    var primaryAssetFieldId = 1;
    var primaryAssetFieldName = "testPrimaryAssetFieldName";
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    when(applicationService.generateApplicationShortReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(ApplicationContext.newBuilder()
        .withPrimaryAsset(primaryAssetFieldJson)
        .withPrimaryOperator(primaryOperatorName)
        .build());
    when(organisationUnitService.getOrganisationUnitRegisteredNumberOrForeignRegisteredNumber(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit registered number lookup for payment metadata"
    )).thenReturn(Optional.empty());

    assertThat(applicationPaymentService.getPaymentMetadata(applicationVersion)).containsExactly(
        entry("Application reference", applicationReference),
        entry("Primary operator", primaryOperatorName),
        entry("Primary operator reg number", ""),
        entry("Primary field", primaryAssetFieldName)
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
  void getPaymentDtos_withApplicationVersions() {
    var applicationVersion1 = ApplicationTestUtil.getNewApplicationVersionWithIdAndType(1, ApplicationType.PRODUCTION);
    var applicationVersion2 = ApplicationTestUtil.getNewApplicationVersionWithIdAndType(2, ApplicationType.PRODUCTION);

    var applicationVersions = List.of(
        applicationVersion1,
        applicationVersion2
    );

    var paymentItemReference1 = "testPaymentItemReference1";
    var paymentItemReference2 = "testPaymentItemReference2";

    var paymentItemReferences = List.of(paymentItemReference1, paymentItemReference2);

    var paymentDtos = List.of(mock(PaymentDto.class), mock(PaymentDto.class));

    doReturn(paymentItemReference1).when(applicationPaymentService).getPaymentItemReference(applicationVersion1);
    doReturn(paymentItemReference2).when(applicationPaymentService).getPaymentItemReference(applicationVersion2);

    when(paymentService.getPaymentDtos(paymentItemReferences, ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE))
        .thenReturn(paymentDtos);

    assertThat(applicationPaymentService.getPaymentDtos(applicationVersions)).isEqualTo(paymentDtos);
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
  void getApplicationVersionIdFromPaymentDto() {
    var paymentDto = mock(PaymentDto.class);

    var itemReference = "1";

    when(paymentDto.itemReference()).thenReturn(itemReference);

    assertThat(applicationPaymentService.getApplicationVersionIdFromPaymentDto(paymentDto))
        .isEqualTo(1);
  }

  @Test
  void getApplicationVersionFromPaymentDto() {
    var paymentDto = mock(PaymentDto.class);

    var applicationVersionId = 1;

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    doReturn(applicationVersionId).when(applicationPaymentService).getApplicationVersionIdFromPaymentDto(paymentDto);

    when(applicationVersionService.getApplicationVersionById(applicationVersionId))
        .thenReturn(applicationVersion);

    assertThat(applicationPaymentService.getApplicationVersionFromPaymentDto(paymentDto))
        .isEqualTo(applicationVersion);
  }

  @Test
  void onPaymentReconcileSuccessEvent_itemTypeNotApplicationVersionPaymentItemType() {
    var paymentDto = mock(PaymentDto.class);

    when(paymentDto.itemType()).thenReturn("testItemType");

    assertThatThrownBy(() -> applicationPaymentService.onPaymentReconcileSuccessEvent(paymentDto))
        .isInstanceOf(IllegalStateException.class);

    verify(applicationSubmissionService, never()).submitApplication(any(), any());
  }

  @Test
  void onPaymentReconcileSuccessEvent_applicationStatusNotAwaitingPayment() {
    var paymentDto = mock(PaymentDto.class);

    var applicationVersion =
        ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(paymentDto.itemType()).thenReturn(ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE);

    doReturn(applicationVersion).when(applicationPaymentService).getApplicationVersionFromPaymentDto(paymentDto);

    assertThatThrownBy(() -> applicationPaymentService.onPaymentReconcileSuccessEvent(paymentDto))
        .isInstanceOf(IllegalStateException.class);

    verify(applicationSubmissionService, never()).submitApplication(any(), any());
  }

  @Test
  void onPaymentReconcileSuccessEvent() {
    var paymentDto = mock(PaymentDto.class);

    var createdByUserId = "1";
    var applicationVersion =
        ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var energyPortalUserDto = mock(EnergyPortalUserDto.class);

    when(paymentDto.itemType()).thenReturn(ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE);
    when(paymentDto.createdByUserId()).thenReturn(createdByUserId);

    doReturn(applicationVersion).when(applicationPaymentService).getApplicationVersionFromPaymentDto(paymentDto);
    when(energyPortalUserService.getByWuaId(WebUserAccountId.valueOf(createdByUserId))).thenReturn(energyPortalUserDto);
    when(energyPortalUserDto.webUserAccountId()).thenReturn(Long.valueOf(createdByUserId));

    applicationPaymentService.onPaymentReconcileSuccessEvent(paymentDto);

    var userCaptor = ArgumentCaptor.forClass(ServiceUserDetail.class);

    verify(applicationSubmissionService).submitApplication(eq(applicationVersion), userCaptor.capture());

    assertThat(userCaptor.getValue().wuaId()).isEqualTo(Long.valueOf(createdByUserId));
  }
}

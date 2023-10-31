package uk.co.nstauthority.fieldconsents.application.payment;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.fivium.digitalpaymentslibrary.payment.CreateCardPaymentResult;
import uk.co.fivium.digitalpaymentslibrary.payment.Payment;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.fee.FeeLineMnemonic;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class ApplicationPaymentServiceTest {

  @Mock
  private ApplicationService applicationService;

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

  @InjectMocks
  @Spy
  private ApplicationPaymentService applicationPaymentService;

  @Test
  void getPaymentAmountPence() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();

    var primaryAsset = new ApplicationAsset();
    var primaryAssetType = AssetType.FIELD;

    var consentLengthDetails = new ConsentLengthDetails();
    var consentLength = ConsentLengthType.SHORT_TERM;
    consentLengthDetails.setConsentLength(consentLength);

    var mnemonic = FeeLineMnemonic.from(
        primaryAssetType,
        application.getType(),
        consentLength,
        ConsentRevisionType.from(application)
    );

    var currentCostPence = 93000;

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(applicationAssetService.getAssetType(primaryAsset)).thenReturn(primaryAssetType);
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
  void getPaymentDescription_primaryAssetIsField() {
    var applicationVersion
        = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryAsset = new ApplicationAsset();
    var primaryAssetType = AssetType.FIELD;

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var applicationReference = "testApplicationReference";

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    when(applicationAssetService.getAssetType(primaryAsset)).thenReturn(primaryAssetType);

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
    var primaryAssetType = AssetType.TERMINAL;

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var applicationReference = "testApplicationReference";

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);
    when(applicationAssetService.getAssetType(primaryAsset)).thenReturn(primaryAssetType);

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
    primaryAsset.setFieldId(primaryAssetFieldId);
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
    primaryAsset.setFieldId(primaryAssetFieldId);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAssetFieldId = 2;
    var secondaryAssetFieldName = "testSecondaryAssetFieldName";
    var secondaryAsset = new ApplicationAsset();
    secondaryAsset.setFieldId(secondaryAssetFieldId);
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
    primaryAsset.setFieldId(primaryAssetFieldId);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAsset1FieldId = 2;
    var secondaryAsset1FieldName = "testSecondaryAsset1FieldName";
    var secondaryAsset1 = new ApplicationAsset();
    secondaryAsset1.setFieldId(secondaryAsset1FieldId);
    secondaryAsset1.setAssetRole(AssetRole.SECONDARY);
    var secondaryAsset1FieldJson = new FieldJson(secondaryAsset1FieldId, secondaryAsset1FieldName, null, null, null);

    var secondaryAsset2FieldId = 3;
    var secondaryAsset2FieldName = "testSecondaryAsset2FieldName";
    var secondaryAsset2 = new ApplicationAsset();
    secondaryAsset2.setFieldId(secondaryAsset2FieldId);
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
    primaryAsset.setFieldId(primaryAssetFieldId);
    primaryAsset.setAssetRole(AssetRole.PRIMARY);
    var primaryAssetFieldJson = new FieldJson(primaryAssetFieldId, primaryAssetFieldName, null, null, null);

    var secondaryAssetTerminalId = 2;
    var secondaryAsset = new ApplicationAsset();
    secondaryAsset.setAssetRole(AssetRole.SECONDARY);
    secondaryAsset.setTerminalId(secondaryAssetTerminalId);

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
    primaryAsset.setTerminalId(primaryAssetTerminalId);
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

    var payment = mock(Payment.class);

    when(payment.getItemReference()).thenReturn("otherPaymentItemReference");
    when(paymentService.getPaymentOrThrow(paymentId)).thenReturn(payment);

    doReturn("testPaymentItemReference").when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    assertThat(applicationPaymentService.isPaymentForApplicationVersion(paymentId, applicationVersion)).isFalse();
  }

  @Test
  void isPaymentForApplicationVersion_itemTypeDoesNotEqualApplicationVersionItemType() {
    var paymentId = UUID.randomUUID();
    var applicationVersion = new ApplicationVersion();

    var payment = mock(Payment.class);
    var paymentItemReference = "testPaymentItemReference";

    when(payment.getItemReference()).thenReturn(paymentItemReference);
    when(payment.getItemType()).thenReturn("otherPaymentItemType");

    when(paymentService.getPaymentOrThrow(paymentId)).thenReturn(payment);
    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    assertThat(applicationPaymentService.isPaymentForApplicationVersion(paymentId, applicationVersion)).isFalse();
  }

  @Test
  void isPaymentForApplicationVersion() {
    var paymentId = UUID.randomUUID();
    var applicationVersion = new ApplicationVersion();

    var payment = mock(Payment.class);
    var paymentItemReference = "testPaymentItemReference";

    when(payment.getItemReference()).thenReturn(paymentItemReference);
    when(payment.getItemType()).thenReturn(ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE);

    when(paymentService.getPaymentOrThrow(paymentId)).thenReturn(payment);
    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    assertThat(applicationPaymentService.isPaymentForApplicationVersion(paymentId, applicationVersion)).isTrue();
  }

  @Test
  void getAndRefreshPayments() {
    var applicationVersion = new ApplicationVersion();

    var paymentItemReference = "testPaymentItemReference";

    var payment1 = mock(Payment.class);
    var payment2 = mock(Payment.class);
    var payments = List.of(payment1, payment2);

    doReturn(paymentItemReference).when(applicationPaymentService).getPaymentItemReference(applicationVersion);

    when(paymentService.getPayments(paymentItemReference, ApplicationPaymentService.APPLICATION_VERSION_PAYMENT_ITEM_TYPE))
        .thenReturn(payments);

    when(payment1.isGovUkPayStateFinished()).thenReturn(true);
    when(payment2.isGovUkPayStateFinished()).thenReturn(false);

    assertThat(applicationPaymentService.getAndRefreshPayments(applicationVersion)).isEqualTo(payments);

    verify(paymentService).refreshPayment(payment2);
  }

  @Test
  void cancelUnfinishedPayments() {
    var payment1 = mock(Payment.class);
    var payment2 = mock(Payment.class);
    var payments = List.of(payment1, payment2);

    when(payment1.isGovUkPayStateFinished()).thenReturn(true);
    when(payment2.isGovUkPayStateFinished()).thenReturn(false);

    applicationPaymentService.cancelUnfinishedPayments(payments);

    verify(paymentService).cancelPayment(payment2);
  }
}

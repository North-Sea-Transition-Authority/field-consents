package uk.co.nstauthority.fieldconsents.application.payment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.fivium.digitalpaymentslibrary.payment.CreateCardPaymentResult;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentService;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentrevision.ConsentRevisionType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.fee.FeeLineMnemonic;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
public class ApplicationPaymentService {

  static final String APPLICATION_VERSION_PAYMENT_ITEM_TYPE = "APPLICATION_VERSION";

  private final ApplicationService applicationService;
  private final ApplicationAssetService applicationAssetService;
  private final ConsentLengthService consentLengthService;
  private final OrganisationUnitService organisationUnitService;
  private final FieldService fieldService;
  private final TerminalService terminalService;
  private final PaymentService paymentService;
  private final FeePeriodService feePeriodService;

  @Autowired
  ApplicationPaymentService(
      ApplicationService applicationService,
      ApplicationAssetService applicationAssetService,
      ConsentLengthService consentLengthService,
      OrganisationUnitService organisationUnitService,
      FieldService fieldService,
      TerminalService terminalService,
      PaymentService paymentService,
      FeePeriodService feePeriodService
  ) {
    this.applicationService = applicationService;
    this.applicationAssetService = applicationAssetService;
    this.consentLengthService = consentLengthService;
    this.organisationUnitService = organisationUnitService;
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.paymentService = paymentService;
    this.feePeriodService = feePeriodService;
  }

  int getPaymentAmountPence(ApplicationVersion applicationVersion) {
    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    var application = applicationVersion.getApplication();
    var consentLength = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

    var mnemonic = FeeLineMnemonic.from(
        applicationAssetService.getAssetType(primaryAsset),
        application.getType(),
        consentLength,
        ConsentRevisionType.from(application)
    );

    return feePeriodService.getCurrentCost(mnemonic.mnemonic());
  }

  CreateCardPaymentResult createPayment(
      ApplicationVersion applicationVersion,
      ServiceUserDetail user,
      Function<UUID, String> returnUrlFunction
  ) {
    return paymentService.createCardPayment(
        getPaymentItemReference(applicationVersion),
        APPLICATION_VERSION_PAYMENT_ITEM_TYPE,
        getPaymentAmountPence(applicationVersion),
        getPaymentDescription(applicationVersion),
        getPaymentMetadata(applicationVersion),
        returnUrlFunction,
        user.wuaId().toString()
    );
  }

  String getPaymentItemReference(ApplicationVersion applicationVersion) {
    return applicationVersion.getId().toString();
  }

  String getPaymentDescription(ApplicationVersion applicationVersion) {
    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    var primaryAssetType = applicationAssetService.getAssetType(primaryAsset);

    var duration = consentLengthService.getConsentLengthDetails(applicationVersion)
        .getConsentLength()
        .getDisplayName()
        .toLowerCase();

    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    return "New %s %s application %s submission"
        .formatted(primaryAssetType.getDisplayName().toLowerCase(), duration, applicationReference);
  }

  Map<String, Object> getPaymentMetadata(ApplicationVersion applicationVersion) {
    var metadata = new LinkedHashMap<String, Object>();

    var primaryOperatorOrganisationUnitJson = organisationUnitService.getOrganisationUnitById(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation lookup for payment metadata"
    );

    metadata.put("Primary operator", primaryOperatorOrganisationUnitJson.name());

    var assets = applicationAssetService.findAssetsByApplicationVersion(applicationVersion);

    var primaryAsset = assets.stream()
        .filter(asset -> asset.getAssetRole() == AssetRole.PRIMARY)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find primary asset"));

    if (primaryAsset.isField()) {
      var primaryFieldId = primaryAsset.getFieldId();
      var secondaryFieldIds = assets
          .stream()
          .filter(applicationAsset -> applicationAsset.getAssetRole() == AssetRole.SECONDARY)
          .filter(ApplicationAsset::isField)
          .map(ApplicationAsset::getFieldId)
          .toList();

      var fieldIds = new ArrayList<Integer>();
      fieldIds.add(primaryFieldId);
      fieldIds.addAll(secondaryFieldIds);

      var fieldJsonsByFieldId = fieldService.findFieldsByIds(fieldIds, "Looking up field names for payment metadata")
          .stream()
          .collect(Collectors.toMap(FieldJson::getId, Function.identity()));

      var primaryFieldJson = Optional.ofNullable(fieldJsonsByFieldId.get(primaryFieldId))
          .orElseThrow(() ->
              new IllegalStateException("No FieldJson returned for field ID %d".formatted(primaryFieldId))
          );

      metadata.put("Primary field", primaryFieldJson.getName());

      if (!secondaryFieldIds.isEmpty()) {
        var additionalFields = secondaryFieldIds.stream()
            .map(secondaryFieldId -> Optional.ofNullable(fieldJsonsByFieldId.get(secondaryFieldId))
                .orElseThrow(() ->
                    new IllegalStateException("No FieldJson returned for field ID %d".formatted(secondaryFieldId))
                )
            )
            .map(FieldJson::getName)
            .collect(Collectors.joining(", "));

        if (secondaryFieldIds.size() == 1) {
          metadata.put("Additional field", additionalFields);
        } else {
          metadata.put("Additional fields", additionalFields);
        }
      }

      return metadata;
    }

    if (primaryAsset.isTerminal()) {
      var primaryTerminalId = primaryAsset.getTerminalId();
      var terminalJson = terminalService.getTerminal(primaryTerminalId, "Looking up terminal name for payment metadata");

      metadata.put("Facility", terminalJson.getName());

      return metadata;
    }

    throw new IllegalStateException("Primary asset %d is not a field or terminal".formatted(primaryAsset.getId()));
  }

  boolean isPaymentForApplicationVersion(UUID paymentId, ApplicationVersion applicationVersion) {
    var payment = paymentService.getPaymentOrThrow(paymentId);
    return payment.getItemReference().equals(getPaymentItemReference(applicationVersion))
        && payment.getItemType().equals(APPLICATION_VERSION_PAYMENT_ITEM_TYPE);
  }

  PaymentStatus handlePaymentProcessed(UUID paymentId) {
    return paymentService.processPaymentCallback(paymentId);
  }
}

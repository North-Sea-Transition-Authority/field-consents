package uk.co.nstauthority.fieldconsents.application;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.fieldconsents.epmqmessage.ApplicationSubmissionType;
import uk.co.nstauthority.fieldconsents.epmqmessage.ApplicationSubmittedFieldConsentsEpmqMessage;
import uk.co.nstauthority.fieldconsents.epmqmessage.FieldConsentsEpmqTopics;

@Service
public class ApplicationSnsService {

  private final ApplicationService applicationService;
  private final ApplicationAssetService applicationAssetService;
  private final SnsService snsService;
  private final Clock clock;
  private final SnsTopicArn applicationsSnsTopicArn;

  ApplicationSnsService(
      ApplicationService applicationService,
      ApplicationAssetService applicationAssetService,
      SnsService snsService,
      Clock clock
  ) {
    this.applicationService = applicationService;
    this.applicationAssetService = applicationAssetService;
    this.snsService = snsService;
    this.clock = clock;

    applicationsSnsTopicArn = snsService.getOrCreateTopic(FieldConsentsEpmqTopics.APPLICATIONS.getName());
  }

  @Async
  public void publishApplicationSubmittedSnsMessage(ApplicationVersion applicationVersion) {
    var primaryAndSecondaryAssets = applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(
        applicationVersion,
        Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
    );

    var primaryAsset = primaryAndSecondaryAssets.stream()
        .filter(applicationAsset -> applicationAsset.getAssetRole() == AssetRole.PRIMARY)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find primary asset for application version: %d"
            .formatted(applicationVersion.getId())));

    Integer primaryFieldId = null;
    List<Integer> secondaryFieldIds = null;
    Integer primaryTerminalId = null;

    var primaryAssetType = primaryAsset.getAssetType();

    switch (primaryAssetType) {
      case FIELD -> {
        primaryFieldId = primaryAsset.getId();

        secondaryFieldIds = primaryAndSecondaryAssets.stream()
            .filter(applicationAsset -> applicationAsset.getAssetType() == AssetType.FIELD)
            .filter(applicationAsset -> applicationAsset.getAssetRole() == AssetRole.SECONDARY)
            .map(ApplicationAsset::getId)
            .toList();

        // We want the secondaryFieldIds in the EPMQ message to be null if there are no secondary field ids so that the column in
        // the Oracle database row that is created by EPMQ is null.
        if (secondaryFieldIds.isEmpty()) {
          secondaryFieldIds = null;
        }
      }
      case TERMINAL -> primaryTerminalId = primaryAsset.getId();
      default -> throw new IllegalStateException("Unknown AssetType: %s".formatted(primaryAssetType));
    }

    var submissionType = getApplicationSubmissionType(applicationVersion);

    var correlationId = CorrelationIdUtil.getCorrelationIdFromMdc();

    var epmqMessage = ApplicationSubmittedFieldConsentsEpmqMessage.builder(correlationId, clock.instant())
        .withApplicationVersionId(applicationVersion.getId())
        .withApplicationReference(applicationService.generateApplicationReference(applicationVersion))
        .withPrimaryOperatorOrganisationUnitId(applicationVersion.getPrimaryOperatorOuId())
        .withPrimaryFieldId(primaryFieldId)
        .withSecondaryFieldIds(secondaryFieldIds)
        .withPrimaryTerminalId(primaryTerminalId)
        .withSubmittedInstant(applicationVersion.getSubmittedDateTime())
        .withSubmissionType(submissionType)
        .build();
    snsService.publishMessage(applicationsSnsTopicArn, epmqMessage);
  }

  ApplicationSubmissionType getApplicationSubmissionType(ApplicationVersion applicationVersion) {
    var isRevision = applicationVersion.getApplication().isRevision();

    if (applicationVersion.isFirstVersion() && !isRevision) {
      return ApplicationSubmissionType.INITIAL_APPLICATION;
    } else if (!isRevision) {
      return ApplicationSubmissionType.APPLICATION_UPDATE;
    } else {
      return ApplicationSubmissionType.APPLICATION_REVISION;
    }
  }
}

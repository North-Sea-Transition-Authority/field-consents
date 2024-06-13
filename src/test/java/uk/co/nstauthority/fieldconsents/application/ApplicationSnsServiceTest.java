package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsTopicArn;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmittedEvent;
import uk.co.nstauthority.fieldconsents.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.fieldconsents.epmqmessage.ApplicationSubmissionType;
import uk.co.nstauthority.fieldconsents.epmqmessage.ApplicationSubmittedFieldConsentsEpmqMessage;
import uk.co.nstauthority.fieldconsents.epmqmessage.FieldConsentsEpmqTopics;

@ExtendWith(MockitoExtension.class)
class ApplicationSnsServiceTest {

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private SnsService snsService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private final SnsTopicArn applicationsTopicArn = new SnsTopicArn("test-applications-topic-arn");

  private ApplicationSnsService applicationSnsService;

  @BeforeEach
  void setUp() {
    when(snsService.getOrCreateTopic(FieldConsentsEpmqTopics.APPLICATIONS.getName()))
        .thenReturn(applicationsTopicArn);

    applicationSnsService = spy(new ApplicationSnsService(
        applicationService,
        applicationVersionService,
        applicationAssetService,
        snsService,
        clock
    ));
  }

  @Test
  void handleApplicationSubmitted() {
    var applicationVersionId = 1;

    var applicationSubmittedEvent = new ApplicationSubmittedEvent(this, applicationVersionId);

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationVersionService.getApplicationVersionById(applicationVersionId)).thenReturn(applicationVersion);
    doNothing().when(applicationSnsService).publishApplicationSubmittedSnsMessage(any());

    applicationSnsService.handleApplicationSubmitted(applicationSubmittedEvent);

    verify(applicationSnsService).publishApplicationSubmittedSnsMessage(applicationVersion);
  }

  @Test
  void publishApplicationSubmittedSnsMessage_primaryAssetIsField_withoutSecondaryFieldAssets() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryAndSecondaryAssets = List.of(ApplicationAssetTestUtil.fieldAsset1);

    var applicationReference = "Test/application/reference";

    var submissionType = ApplicationSubmissionType.INITIAL_APPLICATION;

    var correlationId = UUID.randomUUID().toString();

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(primaryAndSecondaryAssets);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    doReturn(submissionType).when(applicationSnsService).getApplicationSubmissionType(applicationVersion);

    CorrelationIdUtil.setCorrelationIdOnMdc(correlationId);

    applicationSnsService.publishApplicationSubmittedSnsMessage(applicationVersion);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(ApplicationSubmittedFieldConsentsEpmqMessage.class);

    verify(snsService).publishMessage(eq(applicationsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage)
        .extracting(
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationVersionId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationReference,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryOperatorOrganisationUnitId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryFieldId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSecondaryFieldIds,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryTerminalId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmittedInstant,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmissionType,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCorrelationId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCreatedInstant
        )
        .containsExactly(
            applicationVersion.getId(),
            applicationReference,
            applicationVersion.getPrimaryOperatorOuId(),
            ApplicationAssetTestUtil.fieldAsset1.getAssetId(),
            null,
            null,
            applicationVersion.getSubmittedDateTime(),
            submissionType,
            correlationId,
            clock.instant()
        );
  }

  @Test
  void publishApplicationSubmittedSnsMessage_primaryAssetIsField_withSecondaryFieldAssets() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryAndSecondaryAssets = List.of(
        ApplicationAssetTestUtil.fieldAsset1,
        ApplicationAssetTestUtil.fieldAsset2,
        ApplicationAssetTestUtil.fieldAsset3
    );

    var applicationReference = "Test/application/reference";

    var submissionType = ApplicationSubmissionType.INITIAL_APPLICATION;

    var correlationId = UUID.randomUUID().toString();

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(primaryAndSecondaryAssets);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    doReturn(submissionType).when(applicationSnsService).getApplicationSubmissionType(applicationVersion);

    CorrelationIdUtil.setCorrelationIdOnMdc(correlationId);

    applicationSnsService.publishApplicationSubmittedSnsMessage(applicationVersion);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(ApplicationSubmittedFieldConsentsEpmqMessage.class);

    verify(snsService).publishMessage(eq(applicationsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage)
        .extracting(
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationVersionId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationReference,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryOperatorOrganisationUnitId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryFieldId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSecondaryFieldIds,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryTerminalId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmittedInstant,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmissionType,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCorrelationId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCreatedInstant
        )
        .containsExactly(
            applicationVersion.getId(),
            applicationReference,
            applicationVersion.getPrimaryOperatorOuId(),
            ApplicationAssetTestUtil.fieldAsset1.getAssetId(),
            List.of(ApplicationAssetTestUtil.fieldAsset2.getAssetId(), ApplicationAssetTestUtil.fieldAsset3.getAssetId()),
            null,
            applicationVersion.getSubmittedDateTime(),
            submissionType,
            correlationId,
            clock.instant()
        );
  }

  @Test
  void publishApplicationSubmittedSnsMessage_primaryAssetIsField_terminalAssetNotIncluded() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryAndSecondaryAssets = List.of(
        ApplicationAssetTestUtil.fieldAsset1,
        ApplicationAssetTestUtil.fieldAsset2,
        ApplicationAssetTestUtil.terminalAsset1
    );

    var applicationReference = "Test/application/reference";

    var submissionType = ApplicationSubmissionType.INITIAL_APPLICATION;

    var correlationId = UUID.randomUUID().toString();

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(primaryAndSecondaryAssets);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    doReturn(submissionType).when(applicationSnsService).getApplicationSubmissionType(applicationVersion);

    CorrelationIdUtil.setCorrelationIdOnMdc(correlationId);

    applicationSnsService.publishApplicationSubmittedSnsMessage(applicationVersion);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(ApplicationSubmittedFieldConsentsEpmqMessage.class);

    verify(snsService).publishMessage(eq(applicationsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage)
        .extracting(
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationVersionId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationReference,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryOperatorOrganisationUnitId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryFieldId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSecondaryFieldIds,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryTerminalId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmittedInstant,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmissionType,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCorrelationId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCreatedInstant
        )
        .containsExactly(
            applicationVersion.getId(),
            applicationReference,
            applicationVersion.getPrimaryOperatorOuId(),
            ApplicationAssetTestUtil.fieldAsset1.getAssetId(),
            List.of(ApplicationAssetTestUtil.fieldAsset2.getAssetId()),
            null,
            applicationVersion.getSubmittedDateTime(),
            submissionType,
            correlationId,
            clock.instant()
        );
  }

  @Test
  void publishApplicationSubmittedSnsMessage_primaryAssetIsTerminal() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var primaryAndSecondaryAssets = List.of(ApplicationAssetTestUtil.terminalAsset1);

    var applicationReference = "Test/application/reference";

    var submissionType = ApplicationSubmissionType.INITIAL_APPLICATION;

    var correlationId = UUID.randomUUID().toString();

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(primaryAndSecondaryAssets);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    doReturn(submissionType).when(applicationSnsService).getApplicationSubmissionType(applicationVersion);

    CorrelationIdUtil.setCorrelationIdOnMdc(correlationId);

    applicationSnsService.publishApplicationSubmittedSnsMessage(applicationVersion);

    var epmqMessageArgumentCaptor = ArgumentCaptor.forClass(ApplicationSubmittedFieldConsentsEpmqMessage.class);

    verify(snsService).publishMessage(eq(applicationsTopicArn), epmqMessageArgumentCaptor.capture());

    var epmqMessage = epmqMessageArgumentCaptor.getValue();

    assertThat(epmqMessage)
        .extracting(
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationVersionId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getApplicationReference,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryOperatorOrganisationUnitId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryFieldId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSecondaryFieldIds,
            ApplicationSubmittedFieldConsentsEpmqMessage::getPrimaryTerminalId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmittedInstant,
            ApplicationSubmittedFieldConsentsEpmqMessage::getSubmissionType,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCorrelationId,
            ApplicationSubmittedFieldConsentsEpmqMessage::getCreatedInstant
        )
        .containsExactly(
            applicationVersion.getId(),
            applicationReference,
            applicationVersion.getPrimaryOperatorOuId(),
            null,
            null,
            ApplicationAssetTestUtil.terminalAsset1.getAssetId(),
            applicationVersion.getSubmittedDateTime(),
            submissionType,
            correlationId,
            clock.instant()
        );
  }

  @ParameterizedTest
  @CsvSource({
      "1,0,INITIAL_APPLICATION",
      "2,0,APPLICATION_UPDATE",
      "1,1,APPLICATION_REVISION",
      "2,1,APPLICATION_REVISION"
  })
  void getApplicationSubmissionType(int version, int variationNo, ApplicationSubmissionType expectedSubmissionType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersion.setVersion(version);
    applicationVersion.getApplication().setVariationNo(variationNo);

    assertThat(applicationSnsService.getApplicationSubmissionType(applicationVersion)).isEqualTo(expectedSubmissionType);
  }
}

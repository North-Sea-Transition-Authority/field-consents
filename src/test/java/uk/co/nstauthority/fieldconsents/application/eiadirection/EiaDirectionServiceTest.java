package uk.co.nstauthority.fieldconsents.application.eiadirection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting.NeedsSubmittingForm;
import uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose.ProjectPurposeForm;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationJson;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@ExtendWith(MockitoExtension.class)
class EiaDirectionServiceTest {

  private static final int EIA_ID = 1;
  private static final boolean FOR_PURPOSE_OF_EIA_REGS = true;
  private static final boolean HAVE_SUBMITTED_EIA = false;
  private static final int SAT_ID = 2;
  private static final String CACHED_SAT_ID = "ref-2";

  @Mock
  private EiaDirectionRepository eiaDirectionRepository;

  @InjectMocks
  private EiaDirectionService eiaDirectionService;

  @Captor
  private ArgumentCaptor<EiaDirection> eiaDirectionCaptor;

  private ApplicationVersion applicationVersion;

  private EiaDirection emptyEiaDirection;

  private EiaDirection filledInEiaDirection;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    emptyEiaDirection = EiaDirectionBuilder.newBuilder()
        .withId(EIA_ID)
        .withApplicationVersion(applicationVersion)
        .build();

    filledInEiaDirection = EiaDirectionBuilder.from(emptyEiaDirection)
        .withForPurposeOfEiaRegs(FOR_PURPOSE_OF_EIA_REGS)
        .withHaveSubmittedEiaDirection(HAVE_SUBMITTED_EIA)
        .withSatId(SAT_ID)
        .withCachedSatRef(CACHED_SAT_ID)
        .withHaveEiaDirectionToSubmit(true)
        .withLatestDateToBeSubmitted(LocalDate.now())
        .withWhyNoEiaDirection("reason")
        .build();
  }

  @Test
  void getEiaDirectionRestUrl() {
    assertThat(eiaDirectionService.getEiaDirectionRestUrl()).isEqualTo("/data-sources/eia-directions");
  }

  @Test
  void isEiaScreeningStarted() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(EiaDirectionBuilder.newBuilder().build()));
    assertThat(eiaDirectionService.isEiaDirectionStarted(applicationVersion)).isTrue();
  }

  @Test
  void isEiaScreeningStarted_notStarted() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());
    assertThat(eiaDirectionService.isEiaDirectionStarted(applicationVersion)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("isEiaScreeningCompletedParams")
  void isEiaScreeningCompleted(boolean isCompleted, EiaDirection eiaDirection) {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.ofNullable(eiaDirection));
    assertThat(eiaDirectionService.isEiaDirectionCompleted(applicationVersion)).isEqualTo(isCompleted);
  }

  private static Stream<Arguments> isEiaScreeningCompletedParams() {
    return Stream.of(
        Arguments.of(false, null),
        Arguments.of(false, EiaDirectionBuilder.newBuilder().withForPurposeOfEiaRegs(true).build()),
        Arguments.of(true, EiaDirectionBuilder.newBuilder().withForPurposeOfEiaRegs(false).build()),
        Arguments.of(true, EiaDirectionBuilder.newBuilder().withForPurposeOfEiaRegs(true).withSatId(123).build()),
        Arguments.of(true, EiaDirectionBuilder.newBuilder().withForPurposeOfEiaRegs(true).withHaveEiaDirectionToSubmit(false).build()),
        Arguments.of(true, EiaDirectionBuilder.newBuilder().withForPurposeOfEiaRegs(true).withHaveEiaDirectionToSubmit(true).build())
    );
  }

  @Test
  void isEiaScreeningCompleted_noEiaDirection() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    assertThat(eiaDirectionService.isEiaDirectionCompleted(applicationVersion)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void updateEiaDirection_projectPurpose(boolean forPurposeOfEiaRegs) {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(filledInEiaDirection));

    filledInEiaDirection.setForPurposeOfEiaRegs(null);
    eiaDirectionService.updateEiaDirection(applicationVersion, new ProjectPurposeForm(forPurposeOfEiaRegs));

    verify(eiaDirectionRepository).save(eiaDirectionCaptor.capture());
    assertThat(eiaDirectionCaptor.getValue())
        .extracting(
            EiaDirection::getId,
            EiaDirection::getApplicationVersion,
            EiaDirection::getForPurposeOfEiaRegs,
            EiaDirection::getHaveSubmittedEiaDirection,
            EiaDirection::getSatId,
            EiaDirection::getCachedSatRef,
            EiaDirection::getHaveEiaDirectionToSubmit,
            EiaDirection::getLatestDateToBeSubmitted,
            EiaDirection::getWhyNoEiaDirection
        ).containsExactly(
            EIA_ID,
            applicationVersion,
            forPurposeOfEiaRegs,
            null,
            null,
            null,
            null,
            null,
            null
        );
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void updateEiaDirection_projectPurpose_withoutExistingEia(boolean forPurposeOfEiaRegs) {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    eiaDirectionService.updateEiaDirection(applicationVersion, new ProjectPurposeForm(forPurposeOfEiaRegs));

    verify(eiaDirectionRepository).save(eiaDirectionCaptor.capture());
    assertThat(eiaDirectionCaptor.getValue())
        .extracting(
            EiaDirection::getApplicationVersion,
            EiaDirection::getForPurposeOfEiaRegs,
            EiaDirection::getHaveSubmittedEiaDirection,
            EiaDirection::getSatId,
            EiaDirection::getCachedSatRef,
            EiaDirection::getHaveEiaDirectionToSubmit,
            EiaDirection::getLatestDateToBeSubmitted,
            EiaDirection::getWhyNoEiaDirection
        ).containsExactly(
            applicationVersion,
            forPurposeOfEiaRegs,
            null,
            null,
            null,
            null,
            null,
            null
        );
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void updateEiaDirection_projectPurpose_savedWithoutChanges(boolean forPurposeOfEiaRegs) {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(filledInEiaDirection));

    filledInEiaDirection.setForPurposeOfEiaRegs(forPurposeOfEiaRegs);
    eiaDirectionService.updateEiaDirection(applicationVersion, new ProjectPurposeForm(forPurposeOfEiaRegs));

    verify(eiaDirectionRepository, never()).save(any());
  }

  @Test
  void updateEiaDirection_haveSubmittedForm_haveSubmittedEia() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(filledInEiaDirection));

    filledInEiaDirection.setHaveSubmittedEiaDirection(null);
    filledInEiaDirection.setSatId(null);

    var petsApplication = PetsApplicationJson.fromCachedInformation(SAT_ID, CACHED_SAT_ID);
    eiaDirectionService.updateEiaDirection(applicationVersion, true, petsApplication);

    verify(eiaDirectionRepository).save(eiaDirectionCaptor.capture());
    assertThat(eiaDirectionCaptor.getValue())
        .extracting(
            EiaDirection::getId,
            EiaDirection::getApplicationVersion,
            EiaDirection::getForPurposeOfEiaRegs,
            EiaDirection::getHaveSubmittedEiaDirection,
            EiaDirection::getSatId,
            EiaDirection::getCachedSatRef,
            EiaDirection::getHaveEiaDirectionToSubmit,
            EiaDirection::getLatestDateToBeSubmitted,
            EiaDirection::getWhyNoEiaDirection
        ).containsExactly(
            EIA_ID,
            applicationVersion,
            FOR_PURPOSE_OF_EIA_REGS,
            true,
            SAT_ID,
            CACHED_SAT_ID,
            null,
            null,
            null
        );
  }

  @Test
  void updateEiaDirection_haveSubmittedForm_haveNotSubmittedEia() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(filledInEiaDirection));

    filledInEiaDirection.setHaveSubmittedEiaDirection(null);

    eiaDirectionService.updateEiaDirection(applicationVersion, false, null);

    verify(eiaDirectionRepository).save(eiaDirectionCaptor.capture());
    assertThat(eiaDirectionCaptor.getValue())
        .extracting(
            EiaDirection::getId,
            EiaDirection::getApplicationVersion,
            EiaDirection::getForPurposeOfEiaRegs,
            EiaDirection::getHaveSubmittedEiaDirection,
            EiaDirection::getSatId,
            EiaDirection::getCachedSatRef,
            EiaDirection::getHaveEiaDirectionToSubmit,
            EiaDirection::getLatestDateToBeSubmitted,
            EiaDirection::getWhyNoEiaDirection
        ).containsExactly(
            EIA_ID,
            applicationVersion,
            FOR_PURPOSE_OF_EIA_REGS,
            false,
            null,
            null,
            null,
            null,
            null
        );
  }

  @Test
  void updateEiaDirection_haveSubmittedForm_eiaDoesNotExist() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> eiaDirectionService.updateEiaDirection(applicationVersion, false, null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("An EIA direction does not exist for application version %s".formatted(applicationVersion.getId()));
  }

  @Test
  void updateEiaDirection_needsSubmittingForm_doesNeedSubmitting() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(filledInEiaDirection));

    var form = new NeedsSubmittingForm(true, null, null);
    form.latestDateToBeSubmitted().setDate(LocalDate.now().plusWeeks(1));

    filledInEiaDirection.setHaveEiaDirectionToSubmit(null);
    filledInEiaDirection.setLatestDateToBeSubmitted(null);
    filledInEiaDirection.setWhyNoEiaDirection(null);
    eiaDirectionService.updateEiaDirection(applicationVersion, form);

    verify(eiaDirectionRepository).save(eiaDirectionCaptor.capture());
    assertThat(eiaDirectionCaptor.getValue())
        .extracting(
            EiaDirection::getId,
            EiaDirection::getApplicationVersion,
            EiaDirection::getForPurposeOfEiaRegs,
            EiaDirection::getHaveSubmittedEiaDirection,
            EiaDirection::getSatId,
            EiaDirection::getCachedSatRef,
            EiaDirection::getHaveEiaDirectionToSubmit,
            EiaDirection::getLatestDateToBeSubmitted,
            EiaDirection::getWhyNoEiaDirection
        ).containsExactly(
            EIA_ID,
            applicationVersion,
            FOR_PURPOSE_OF_EIA_REGS,
            HAVE_SUBMITTED_EIA,
            SAT_ID,
            CACHED_SAT_ID,
            form.haveEiaDirectionToSubmit(),
            form.latestDateToBeSubmitted().getAsLocalDate().orElseThrow(),
            form.whyNoEiaDirection().getInputValue()
        );
  }

  @Test
  void updateEiaDirection_needsSubmittingForm_doesNotNeedSubmitting() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(filledInEiaDirection));

    var form = new NeedsSubmittingForm(false, null, null);
    form.whyNoEiaDirection().setInputValue("reason");

    filledInEiaDirection.setHaveEiaDirectionToSubmit(null);
    filledInEiaDirection.setLatestDateToBeSubmitted(null);
    filledInEiaDirection.setWhyNoEiaDirection(null);
    eiaDirectionService.updateEiaDirection(applicationVersion, form);

    verify(eiaDirectionRepository).save(eiaDirectionCaptor.capture());
    assertThat(eiaDirectionCaptor.getValue())
        .extracting(
            EiaDirection::getId,
            EiaDirection::getApplicationVersion,
            EiaDirection::getForPurposeOfEiaRegs,
            EiaDirection::getHaveSubmittedEiaDirection,
            EiaDirection::getSatId,
            EiaDirection::getCachedSatRef,
            EiaDirection::getHaveEiaDirectionToSubmit,
            EiaDirection::getLatestDateToBeSubmitted,
            EiaDirection::getWhyNoEiaDirection
        ).containsExactly(
            EIA_ID,
            applicationVersion,
            FOR_PURPOSE_OF_EIA_REGS,
            HAVE_SUBMITTED_EIA,
            SAT_ID,
            CACHED_SAT_ID,
            form.haveEiaDirectionToSubmit(),
            null,
            form.whyNoEiaDirection().getInputValue()
        );
  }

  @Test
  void updateEiaDirection_needsSubmittingForm_eiaDoesNotExist() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var form = NeedsSubmittingForm.empty();
    assertThatThrownBy(() -> eiaDirectionService.updateEiaDirection(applicationVersion, form))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("An EIA direction does not exist for application version 1");
  }

  @Test
  void findEiaDirection_notFound() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    var eiaDirectionOptional = eiaDirectionService.findEiaDirection(applicationVersion);
    assertThat(eiaDirectionOptional).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("getEiaDirectionSummaryCardParams")
  void getEiaDirectionSummaryCard(EiaDirection eiaDirection, SummaryCard summaryCard) {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.ofNullable(eiaDirection));

    assertThat(eiaDirectionService.getEiaDirectionSummaryCard(applicationVersion)).isEqualTo(summaryCard);
  }

  private static Stream<Arguments> getEiaDirectionSummaryCardParams() {
    var projectPurposeQuestion = "Is this a \"project\" for the purposes of EIA Regulations 2020?";
    var haveSubmittedQuestion = "Have you submitted an EIA screening direction to the Secretary of State or OPRED?";
    var satIdQuestion = "EIA screening direction reference";
    var needsSubmittingQuestion = "Do you have an EIA screening direction that still needs to be submitted?";
    var latestSubmissionQuestion = "What is the latest date this will be submitted?";
    var whyNoSubmissionQuestion = "Explain why you don’t intend to submit an EIA screening direction";

    var weekFromNow = LocalDate.now().plusWeeks(1);

    return Stream.of(
        Arguments.of(null, SummaryCard.emptySummaryCard()),
        Arguments.of(
            EiaDirectionBuilder.newBuilder().build(),
            SummaryCard.simpleSummaryCard(SummaryDataView.newWithKeyValue(projectPurposeQuestion, null))
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder().withForPurposeOfEiaRegs(true).build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, null)
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder().withForPurposeOfEiaRegs(false).build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView.newWithKeyValue(projectPurposeQuestion, false)
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder()
                .withForPurposeOfEiaRegs(true)
                .withHaveSubmittedEiaDirection(false)
                .build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, false)
                    .addKeyValue(needsSubmittingQuestion, null)
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder()
                .withForPurposeOfEiaRegs(true)
                .withHaveSubmittedEiaDirection(true)
                .build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, true)
                    .addKeyValue(satIdQuestion, null)
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder()
                .withForPurposeOfEiaRegs(true)
                .withHaveSubmittedEiaDirection(true)
                .withCachedSatRef(CACHED_SAT_ID)
                .build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, true)
                    .addKeyValue(satIdQuestion, CACHED_SAT_ID)
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder()
                .withForPurposeOfEiaRegs(true)
                .withHaveSubmittedEiaDirection(false)
                .withHaveEiaDirectionToSubmit(true)
                .build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, false)
                    .addKeyValue(needsSubmittingQuestion, true)
                    .addKeyValue(latestSubmissionQuestion, null)
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder()
                .withForPurposeOfEiaRegs(true)
                .withHaveSubmittedEiaDirection(false)
                .withHaveEiaDirectionToSubmit(false)
                .build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, false)
                    .addKeyValue(needsSubmittingQuestion, false)
                    .addKeyValue(whyNoSubmissionQuestion, null)
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder()
                .withForPurposeOfEiaRegs(true)
                .withHaveSubmittedEiaDirection(false)
                .withHaveEiaDirectionToSubmit(false)
                .withWhyNoEiaDirection("reason")
                .build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, false)
                    .addKeyValue(needsSubmittingQuestion, false)
                    .addKeyValue(whyNoSubmissionQuestion, "reason")
            )
        ),
        Arguments.of(
            EiaDirectionBuilder.newBuilder()
                .withForPurposeOfEiaRegs(true)
                .withHaveSubmittedEiaDirection(false)
                .withHaveEiaDirectionToSubmit(true)
                .withLatestDateToBeSubmitted(weekFromNow)
                .build(),
            SummaryCard.simpleSummaryCard(
                SummaryDataView
                    .newWithKeyValue(projectPurposeQuestion, true)
                    .addKeyValue(haveSubmittedQuestion, false)
                    .addKeyValue(needsSubmittingQuestion, true)
                    .addKeyValue(latestSubmissionQuestion, weekFromNow)
            )
        )
    );
  }

}

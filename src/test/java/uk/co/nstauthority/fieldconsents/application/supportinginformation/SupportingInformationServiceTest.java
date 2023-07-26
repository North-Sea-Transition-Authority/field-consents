package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileUsage;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class SupportingInformationServiceTest {

  private static final String APPLICATION_NOTES_PROMPT = "Notes";

  static final String APPLICATION_NOTES = "application notes";

  static final String ERAP_NOTES = "erap notes";

  @Mock
  private SupportingInformationRepository supportingInformationRepository;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  private ApplicationVersion applicationVersion;

  private ApplicationVersionFileUsage fileUsage;

  private SupportingInformation supportingInformation;

  @InjectMocks
  private SupportingInformationService supportingInformationService;

  @Captor
  private ArgumentCaptor<SupportingInformation> supportingInformationCaptor;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    fileUsage = ApplicationVersionFileUsage.supportingDocumentFrom(applicationVersion);
    supportingInformation = getSupportingInformation();
  }

  @Test
  void getSupportingInformationForm_whenNoDataFound() {
    when(supportingInformationRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    SupportingInformationForm newform = supportingInformationService.getSupportingInformationForm(applicationVersion);

    assertThat(newform)
        .usingRecursiveComparison()
        .isEqualTo(new SupportingInformationForm());
  }

  @Test
  void getSupportingInformationForm_whenDataFoundForNonProductionApplication() {
    SupportingInformationForm supportingInformationForm = getSupportingInformationForm();
    when(supportingInformationRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(supportingInformation));

    SupportingInformationForm existingForm = supportingInformationService.getSupportingInformationForm(applicationVersion);

    assertThat(existingForm)
        .usingRecursiveComparison()
        .isEqualTo(supportingInformationForm);
  }

  @Test
  void getSupportingInformationForm_whenDataFoundForProductionApplication() {
    SupportingInformationForm supportingInformationForm = getSupportingInformationProductionForm();
    SupportingInformation supportingInformation = getSupportingInformationProduction();

    when(supportingInformationRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(supportingInformation));

    SupportingInformationForm existingForm = supportingInformationService.getSupportingInformationForm(applicationVersion);

    assertThat(existingForm)
        .usingRecursiveComparison()
        .isEqualTo(supportingInformationForm);
  }

  @Test
  void findSupportingInformation_whenNoDataFound() {
    when(supportingInformationRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.empty());

    assertThat(supportingInformationService.findSupportingInformation(applicationVersion)).isNotPresent();
  }

  @Test
  void findSupportingInformation_whenDataFound() {
    when(supportingInformationRepository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(supportingInformation));

    assertThat(supportingInformationService.findSupportingInformation(applicationVersion)).isPresent();
  }

  @Test
  void saveSupportingInformation_whenNonProductionApplication() {
    SupportingInformationForm form = getSupportingInformationForm();

    supportingInformationService.saveSupportingInformation(applicationVersion, form);

    verify(supportingInformationRepository).deleteByApplicationVersion(applicationVersion);
    verify(fieldConsentsFileService).saveDocuments(fileUsage, form.getSupportingDocuments());
    verify(supportingInformationRepository).save(supportingInformationCaptor.capture());

    assertThat(supportingInformationCaptor.getValue())
        .extracting(
            SupportingInformation::getNotes,
            SupportingInformation::getErapNotes
        ).containsExactly(
            supportingInformation.getNotes(),
            supportingInformation.getErapNotes()
        );
  }

  @Test
  void saveSupportingInformation_whenProductionApplication() {
    SupportingInformationForm form = getSupportingInformationProductionForm();

    supportingInformationService.saveSupportingInformation(applicationVersion, form);

    verify(supportingInformationRepository).deleteByApplicationVersion(applicationVersion);
    verify(supportingInformationRepository).save(supportingInformationCaptor.capture());

    assertThat(supportingInformationCaptor.getValue())
        .extracting(
            SupportingInformation::getNotes,
            SupportingInformation::getErapNotes
        )
        .containsExactly(
            supportingInformation.getNotes(),
            null
        );
  }

  @Test
  void getSupportingInformationSummaryCard_noSupportingInfo() {
    when(supportingInformationRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var summaryCard = supportingInformationService.getSupportingInformationSummaryCard(applicationVersion);

    assertThat(summaryCard)
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getSupportingInformationSummaryCard_supportingInfoProduction() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(supportingInformationRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(getSupportingInformationProduction()));

    var summaryCard = supportingInformationService.getSupportingInformationSummaryCard(applicationVersion);

    assertThat(summaryCard)
        .usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(new SummaryKeyValue(APPLICATION_NOTES_PROMPT, APPLICATION_NOTES)))
        ));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void getSupportingInformationSummaryCard_supportingInfoFlare(ApplicationType applicationType) {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    when(supportingInformationRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(getSupportingInformation()));

    var summaryCard = supportingInformationService.getSupportingInformationSummaryCard(applicationVersion);

    assertThat(summaryCard)
        .usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue(APPLICATION_NOTES_PROMPT, APPLICATION_NOTES),
                new SummaryKeyValue("ERAP alignment studies and projects", ERAP_NOTES)
            ))
        ));
  }

  private SupportingInformationForm getSupportingInformationForm() {
    SupportingInformationForm supportingInformationForm = getSupportingInformationProductionForm();
    supportingInformationForm.setErapNotes(ERAP_NOTES);
    return supportingInformationForm;
  }

  private SupportingInformationForm getSupportingInformationProductionForm() {
    SupportingInformationForm supportingInformationForm = new SupportingInformationForm();
    supportingInformationForm.setNotes(APPLICATION_NOTES);
    supportingInformationForm.setSupportingDocuments(Collections.emptyList());
    return supportingInformationForm;
  }

  @NotNull
  private SupportingInformation getSupportingInformation() {
    SupportingInformation supportingInformation = getSupportingInformationProduction();
    supportingInformation.setErapNotes(ERAP_NOTES);
    return supportingInformation;
  }

  @NotNull
  private SupportingInformation getSupportingInformationProduction() {
    SupportingInformation supportingInformation = new SupportingInformation();
    supportingInformation.setApplicationVersion(applicationVersion);
    supportingInformation.setNotes(APPLICATION_NOTES);
    return supportingInformation;
  }
}

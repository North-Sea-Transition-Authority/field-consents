package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class SupportingInformationServiceTest {

  static final String APPLICATION_NOTES = "application notes";

  static final String ERAP_NOTES = "erap notes";

  @Mock
  private SupportingInformationRepository supportingInformationRepository;

  private ApplicationVersion applicationVersion;

  private SupportingInformation supportingInformation;

  private SupportingInformationService supportingInformationService;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    supportingInformationService = new SupportingInformationService(supportingInformationRepository);

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

    ArgumentCaptor<SupportingInformation> supportingInformationArgumentCaptor = getSupportingInformationArgumentCaptor(form);

    SupportingInformation savedSupportingInformation = supportingInformationArgumentCaptor.getValue();

    assertThat(savedSupportingInformation.getNotes()).isEqualTo(supportingInformation.getNotes());
    assertThat(savedSupportingInformation.getErapNotes()).isEqualTo(supportingInformation.getErapNotes());
  }

  @Test
  void saveSupportingInformation_whenProductionApplication() {
    SupportingInformationForm form = getSupportingInformationProductionForm();

    ArgumentCaptor<SupportingInformation> supportingInformationArgumentCaptor = getSupportingInformationArgumentCaptor(form);

    SupportingInformation savedSupportingInformation = supportingInformationArgumentCaptor.getValue();

    assertThat(savedSupportingInformation.getNotes()).isEqualTo(supportingInformation.getNotes());
    assertThat(savedSupportingInformation.getErapNotes()).isNull();
  }

  @NotNull
  private ArgumentCaptor<SupportingInformation> getSupportingInformationArgumentCaptor(SupportingInformationForm form) {
    supportingInformationService.saveSupportingInformation(applicationVersion, form);

    verify(supportingInformationRepository, times(1)).deleteByApplicationVersion(applicationVersion);

    ArgumentCaptor<SupportingInformation> supportingInformationArgumentCaptor = ArgumentCaptor.forClass(SupportingInformation.class);
    verify(supportingInformationRepository, times(1)).save(supportingInformationArgumentCaptor.capture());
    return supportingInformationArgumentCaptor;
  }

  private SupportingInformationForm getSupportingInformationForm() {
    SupportingInformationForm supportingInformationForm = getSupportingInformationProductionForm();
    supportingInformationForm.setErapNotes(ERAP_NOTES);
    return supportingInformationForm;
  }

  private SupportingInformationForm getSupportingInformationProductionForm() {
    SupportingInformationForm supportingInformationForm = new SupportingInformationForm();
    supportingInformationForm.setNotes(APPLICATION_NOTES);
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
package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;

@ExtendWith(MockitoExtension.class)
class ConsentDataServiceTest {

  @Mock
  private ConsentDataRepository repository;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private FieldConsentsDocumentInstanceService documentInstanceService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @InjectMocks
  @Spy
  private ConsentDataService consentDataService;

  @Captor
  private ArgumentCaptor<ConsentData> consentDataCaptor;

  private Application application;

  @BeforeEach
  void setUp() {
    application = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE).getApplication();
  }

  @Test
  void findConsentData() {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(consentData));
    assertThat(consentDataService.findConsentData(application)).contains(consentData);
  }

  @Test
  void findConsentData_notFound() {
    when(repository.findByApplication(application)).thenReturn(Optional.empty());
    assertThat(consentDataService.findConsentData(application)).isEmpty();
  }

  @Test
  void saveConsentData_doesNotExistBeforeSaving() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    var form = ConsentDataForm.from(startDate, endDate);

    when(repository.findByApplication(application)).thenReturn(Optional.empty());

    consentDataService.saveConsentData(application, form);

    verify(documentInstanceService).createDocumentInstancesForApplication(application);

    verify(repository).save(consentDataCaptor.capture());
    assertThat(consentDataCaptor.getValue())
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication,
            ConsentData::getConsentStartDate,
            ConsentData::getConsentEndDate
        ).containsExactly(
            null,
            application,
            startDate,
            endDate
        );
  }

  @Test
  void saveConsentData_doesExistBeforeSaving() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    var form = ConsentDataForm.from(startDate, endDate);

    var existingConsentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(existingConsentData));

    consentDataService.saveConsentData(application, form);

    verify(documentInstanceService, never()).createDocumentInstancesForApplication(any());

    verify(repository).save(consentDataCaptor.capture());
    assertThat(consentDataCaptor.getValue())
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication,
            ConsentData::getConsentStartDate,
            ConsentData::getConsentEndDate
        ).containsExactly(
            existingConsentData.getId(),
            application,
            startDate,
            endDate
        );
  }

  @Test
  void getPrefilledConsentDataForm_consentDataExists() {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    doReturn(Optional.of(consentData)).when(consentDataService).findConsentData(application);

    assertThat(consentDataService.getPrefilledConsentDataForm(application))
        .extracting(
            form -> form.consentStartDate().getAsLocalDate().orElseThrow(),
            form -> form.consentEndDate().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate()
        );
  }

  @Test
  void getPrefilledConsentDataForm_consentDataDoesNotExist() {
    var applicationVersion = new ApplicationVersion();

    var consentLengthDetails = new ConsentLengthDetails();

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    doReturn(Optional.empty()).when(consentDataService).findConsentData(application);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);

    assertThat(consentDataService.getPrefilledConsentDataForm(application))
        .extracting(
            form -> form.consentStartDate().getAsLocalDate().orElseThrow(),
            form -> form.consentEndDate().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            proposedConsentStartDate,
            proposedConsentEndDate
        );
  }
}

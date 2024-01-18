package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;

@ExtendWith(MockitoExtension.class)
class ConsentDataServiceTest {

  @Mock
  private ConsentDataRepository repository;

  @Mock
  private FieldConsentsDocumentInstanceService documentInstanceService;

  @InjectMocks
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
    var consentStartDate = LocalDate.parse("2024-01-01");
    var consentEndDate = LocalDate.parse("2025-01-01");

    when(repository.findByApplication(application)).thenReturn(Optional.empty());

    consentDataService.saveConsentData(application, consentStartDate, consentEndDate);

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
            consentStartDate,
            consentEndDate
        );
  }

  @Test
  void saveConsentData_doesExistBeforeSaving() {
    var consentStartDate = LocalDate.parse("2024-01-01");
    var consentEndDate = LocalDate.parse("2025-01-01");

    var existingConsentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(existingConsentData));

    consentDataService.saveConsentData(application, consentStartDate, consentEndDate);

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
            consentStartDate,
            consentEndDate
        );
  }
}

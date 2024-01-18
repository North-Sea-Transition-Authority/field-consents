package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;

@Service
public class ConsentDataService {

  private final ConsentDataRepository repository;
  private final FieldConsentsDocumentInstanceService documentInstanceService;

  ConsentDataService(
      ConsentDataRepository repository,
      FieldConsentsDocumentInstanceService documentInstanceService
  ) {
    this.repository = repository;
    this.documentInstanceService = documentInstanceService;
  }

  public Optional<ConsentData> findConsentData(Application application) {
    return repository.findByApplication(application);
  }

  @Transactional
  public void saveConsentData(Application application, LocalDate consentStartDate, LocalDate consentEndDate) {
    var consentDataOptional = findConsentData(application);

    var consentData = consentDataOptional.orElseGet(ConsentData::new);
    consentData.setApplication(application);
    consentData.setConsentStartDate(consentStartDate);
    consentData.setConsentEndDate(consentEndDate);

    if (consentDataOptional.isEmpty()) {
      documentInstanceService.createDocumentInstancesForApplication(application);
    }

    repository.save(consentData);
  }

}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;

@Service
public class ConsentDataService {

  private final ConsentDataRepository repository;
  private final ConsentLengthService consentLengthService;
  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;
  private final ApplicationVersionService applicationVersionService;

  ConsentDataService(
      ConsentDataRepository repository,
      ConsentLengthService consentLengthService,
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService,
      ApplicationVersionService applicationVersionService) {
    this.repository = repository;
    this.consentLengthService = consentLengthService;
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
    this.applicationVersionService = applicationVersionService;
  }

  public Optional<ConsentData> findConsentData(Application application) {
    return repository.findByApplication(application);
  }

  @Transactional
  public void saveConsentData(Application application, ConsentDataForm form) {
    var consentDataOptional = findConsentData(application);

    var consentData = consentDataOptional.orElseGet(ConsentData::new);
    consentData.setApplication(application);
    consentData.setConsentStartDate(form.consentStartDate().getAsLocalDate().orElseThrow());
    consentData.setConsentEndDate(form.consentEndDate().getAsLocalDate().orElseThrow());

    if (consentDataOptional.isEmpty()) {
      fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);
    }

    repository.save(consentData);
  }

  ConsentDataForm getPrefilledConsentDataForm(Application application) {
    return findConsentData(application)
        .map(consentData -> ConsentDataForm.from(consentData.getConsentStartDate(), consentData.getConsentEndDate()))
        .orElseGet(() -> {
          var applicationVersion =
              applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId());
          var consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

          return ConsentDataForm.from(
              consentLengthService.getProposedConsentStartDate(consentLengthDetails),
              consentLengthService.getProposedConsentEndDate(consentLengthDetails)
          );
        });
  }
}

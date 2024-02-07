package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentEmissionFigureService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;

@Service
public class ConsentDataService {

  private final ApplicationVersionService applicationVersionService;
  private final ConsentDataRepository repository;
  private final ConsentLengthService consentLengthService;
  private final ConsentProductionFiguresService consentProductionFiguresService;
  private final ConsentEmissionFigureService consentEmissionFigureService;
  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  ConsentDataService(
      ApplicationVersionService applicationVersionService,
      ConsentDataRepository repository,
      ConsentLengthService consentLengthService,
      ConsentProductionFiguresService consentProductionFiguresService,
      ConsentEmissionFigureService consentEmissionFigureService,
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.repository = repository;
    this.consentLengthService = consentLengthService;
    this.consentProductionFiguresService = consentProductionFiguresService;
    this.consentEmissionFigureService = consentEmissionFigureService;
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
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

  public ConsentDataView getConsentDataView(
      ApplicationVersion applicationVersion,
      ConsentData consentData,
      ConsentLengthType consentLengthType
  ) {
    return switch (applicationVersion.getApplication().getType()) {
      case PRODUCTION -> switch (consentLengthType) {
        case SHORT_TERM, ANNUAL ->
            getConsentDataViewForShortTermOrAnnualProductionApplication(applicationVersion, consentData, consentLengthType);
        case LONG_TERM -> getConsentDataViewForLongTermProductionApplication(applicationVersion, consentData);
      };
      case FLARE, VENT -> getConsentDataViewForFlareOrVentApplication(applicationVersion, consentData, consentLengthType);
    };
  }

  ConsentDataView getConsentDataViewForShortTermOrAnnualProductionApplication(
      ApplicationVersion applicationVersion,
      ConsentData consentData,
      ConsentLengthType consentLengthType
  ) {
    var shortTermOrAnnualConsentProductionFiguresDto = switch (consentLengthType) {
      case SHORT_TERM -> consentProductionFiguresService.getShortTermConsentProductionFiguresDto(applicationVersion);
      case ANNUAL -> consentProductionFiguresService.getAnnualConsentProductionFiguresDto(applicationVersion);
      default -> throw new IllegalStateException("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
    };

    return ConsentDataView.fromShortTermOrAnnualProductionApplication(
        consentData,
        shortTermOrAnnualConsentProductionFiguresDto
    );
  }

  ConsentDataView getConsentDataViewForLongTermProductionApplication(
      ApplicationVersion applicationVersion,
      ConsentData consentData
  ) {
    var longTermConsentProductionFiguresDtos =
        consentProductionFiguresService.getLongTermConsentProductionFiguresDtos(applicationVersion);

    return ConsentDataView.fromLongTermProductionApplication(consentData, longTermConsentProductionFiguresDtos);
  }

  ConsentDataView getConsentDataViewForFlareOrVentApplication(
      ApplicationVersion applicationVersion,
      ConsentData consentData,
      ConsentLengthType consentLengthType
  ) {
    var emissionMaxRate = switch (consentLengthType) {
      case SHORT_TERM -> consentEmissionFigureService.getShortTermEmissionMaxRate(applicationVersion);
      case ANNUAL -> consentEmissionFigureService.getAnnualEmissionMaxRate(applicationVersion);
      default -> throw new IllegalStateException("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
    };

    return ConsentDataView.fromFlareOrVentApplication(consentData, emissionMaxRate);
  }
}

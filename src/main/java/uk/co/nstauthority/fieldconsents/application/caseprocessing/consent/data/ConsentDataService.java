package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentEmissionFigureService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

@Service
public class ConsentDataService {

  private final ConsentDataRepository repository;
  private final ConsentLengthService consentLengthService;
  private final ConsentProductionFiguresService consentProductionFiguresService;
  private final ConsentEmissionFigureService consentEmissionFigureService;
  private final ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService;
  private final ApplicationVersionService applicationVersionService;

  ConsentDataService(
      ConsentDataRepository repository,
      ConsentLengthService consentLengthService,
      ConsentProductionFiguresService consentProductionFiguresService,
      ConsentEmissionFigureService consentEmissionFigureService,
      ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService,
      ApplicationVersionService applicationVersionService
  ) {
    this.repository = repository;
    this.consentLengthService = consentLengthService;
    this.consentProductionFiguresService = consentProductionFiguresService;
    this.consentEmissionFigureService = consentEmissionFigureService;
    this.consentDataLongTermProductionFiguresService = consentDataLongTermProductionFiguresService;
    this.applicationVersionService = applicationVersionService;
  }

  public Optional<ConsentData> findConsentData(Application application) {
    return repository.findByApplication(application);
  }

  public ConsentData getConsentData(Application application) {
    return findConsentData(application).orElseThrow(() ->
        new IllegalStateException("Unable to find consent data for application: %s".formatted(application.getId()))
    );
  }

  public List<ConsentData> getConsentDataListForCompletedApplicationsWithAssets(
      Collection<ApplicationAsset> applicationAssets
  ) {
    return repository.getConsentDataListForCompletedApplicationsWithAssets(applicationAssets);
  }

  /**
   * We want this event listener to be BEFORE_COMMIT because by default (AFTER_COMMIT) Spring will execute this method
   * after the event producing method's transaction has committed. This means any changes in this method won't be committed.
   */
  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  void onConsentLengthChangeEvent(ConsentLengthChangeEvent event) {
    var applicationVersion = applicationVersionService.getApplicationVersionById(event.getApplicationVersionId());
    var application = applicationVersion.getApplication();

    consentDataLongTermProductionFiguresService.deleteConsentDataLongTermProductionFigures(application);

    repository.deleteByApplication(application);
  }

  @Transactional
  public void saveConsentData(Application application, ConsentLengthType consentLengthType, ConsentDataForm form) {
    var consentDataOptional = findConsentData(application);

    var consentData = consentDataOptional.orElseGet(ConsentData::new);
    consentData.setApplication(application);

    updateConsentDataFromForm(application, consentLengthType, consentData, form);

    repository.save(consentData);

    if (application.getType() == ApplicationType.PRODUCTION && consentLengthType == ConsentLengthType.LONG_TERM) {
      consentDataLongTermProductionFiguresService.saveConsentDataLongTermProductionFigures(application, form);
    }
  }

  void updateConsentDataFromForm(
      Application application,
      ConsentLengthType consentLengthType,
      ConsentData consentData,
      ConsentDataForm form
  ) {
    consentData.setConsentStartDate(form.getConsentStartDateInput().getAsLocalDate().orElseThrow());
    consentData.setConsentEndDate(form.getConsentEndDateInput().getAsLocalDate().orElseThrow());

    var applicationType = application.getType();
    if (applicationType == ApplicationType.PRODUCTION) {
      if (consentLengthType == ConsentLengthType.SHORT_TERM || consentLengthType == ConsentLengthType.ANNUAL) {
        updateConsentDataFromFormForShortTermOrAnnualProductionApplication(consentData, form);
      } else if (consentLengthType == ConsentLengthType.LONG_TERM) {
        updateConsentDataFromFormForLongTermProductionApplication(consentData, form);
      }
      return;
    }

    if (applicationType == ApplicationType.FLARE || applicationType == ApplicationType.VENT) {
      updateConsentDataFromFormForEmissionApplication(consentData, form);
    }
  }

  void updateConsentDataFromFormForShortTermOrAnnualProductionApplication(ConsentData consentData, ConsentDataForm form) {
    var consentProductionFiguresInput = form.getShortTermOrAnnualConsentProductionFiguresInput();
    var consentProductionFiguresDto = consentProductionFiguresInput.getAsDtoOrThrow();

    consentData.setShortTermOrAnnualProductionMinOil(consentProductionFiguresDto.minOil());
    consentData.setShortTermOrAnnualProductionMaxOil(consentProductionFiguresDto.maxOil());
    consentData.setShortTermOrAnnualProductionMinGas(consentProductionFiguresDto.minGas());
    consentData.setShortTermOrAnnualProductionMaxGas(consentProductionFiguresDto.maxGas());
  }

  void updateConsentDataFromFormForLongTermProductionApplication(ConsentData consentData, ConsentDataForm form) {
    var longTermProductionConsentProductionFromDate =
        form.getLongTermProductionConsentProductionFromDateInput().getAsLocalDate().orElseThrow();
    consentData.setLongTermProductionConsentProductionFromDate(longTermProductionConsentProductionFromDate);
  }

  void updateConsentDataFromFormForEmissionApplication(ConsentData consentData, ConsentDataForm form) {
    consentData.setEmissionDailyAverage(form.getEmissionDailyAverageInput().getAsBigDecimal().orElseThrow());
  }

  ConsentDataForm getPrefilledConsentDataForm(ApplicationVersion applicationVersion, ConsentLengthDetails consentLengthDetails) {
    return switch (applicationVersion.getApplication().getType()) {
      case PRODUCTION -> switch (consentLengthDetails.getConsentLength()) {
        case SHORT_TERM, ANNUAL ->
            getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication(applicationVersion, consentLengthDetails);
        case LONG_TERM -> getPrefilledConsentDataFormForLongTermProductionApplication(applicationVersion, consentLengthDetails);
      };
      case FLARE, VENT -> getPrefilledConsentDataFormForEmissionApplication(applicationVersion, consentLengthDetails);
    };
  }

  ConsentDataForm getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication(
      ApplicationVersion applicationVersion,
      ConsentLengthDetails consentLengthDetails
  ) {
    return findConsentData(applicationVersion.getApplication())
        .map(ConsentDataForm::fromShortTermOrAnnualProductionApplication)
        .orElseGet(() -> {
          var proposedConsentStartDate = consentLengthService.getProposedConsentStartDate(consentLengthDetails);
          var proposedConsentEndDate = consentLengthService.getProposedConsentEndDate(consentLengthDetails);

          var consentLengthType = consentLengthDetails.getConsentLength();

          var shortTermOrAnnualConsentProductionFiguresDto = switch (consentLengthType) {
            case SHORT_TERM -> consentProductionFiguresService.getShortTermConsentProductionFiguresDto(applicationVersion);
            case ANNUAL -> consentProductionFiguresService.getAnnualConsentProductionFiguresDto(applicationVersion);
            default -> throw new IllegalStateException("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
          };

          return ConsentDataForm.fromShortTermOrAnnualProductionApplication(
              proposedConsentStartDate,
              proposedConsentEndDate,
              shortTermOrAnnualConsentProductionFiguresDto
          );
        });
  }

  ConsentDataForm getPrefilledConsentDataFormForLongTermProductionApplication(
      ApplicationVersion applicationVersion,
      ConsentLengthDetails consentLengthDetails
  ) {
    return findConsentData(applicationVersion.getApplication())
        .map(consentData -> {
          var consentDataLongTermProductionFiguresList = consentDataLongTermProductionFiguresService
              .getConsentDataLongTermProductionFiguresList(applicationVersion.getApplication());

          return ConsentDataForm.fromLongTermProductionApplication(consentData, consentDataLongTermProductionFiguresList);
        })
        .orElseGet(() -> {
          var proposedConsentStartDate = consentLengthService.getProposedConsentStartDate(consentLengthDetails);
          var proposedConsentEndDate = consentLengthService.getProposedConsentEndDate(consentLengthDetails);

          var longTermConsentProductionFiguresDtos =
              consentProductionFiguresService.getLongTermConsentProductionFiguresDtos(applicationVersion);

          return ConsentDataForm.fromLongTermProductionApplication(
              proposedConsentStartDate,
              proposedConsentEndDate,
              longTermConsentProductionFiguresDtos
          );
        });
  }

  ConsentDataForm getPrefilledConsentDataFormForEmissionApplication(
      ApplicationVersion applicationVersion,
      ConsentLengthDetails consentLengthDetails
  ) {
    return findConsentData(applicationVersion.getApplication())
        .map(ConsentDataForm::fromEmissionApplication)
        .orElseGet(() -> {
          var proposedConsentStartDate = consentLengthService.getProposedConsentStartDate(consentLengthDetails);
          var proposedConsentEndDate = consentLengthService.getProposedConsentEndDate(consentLengthDetails);

          var consentLengthType = consentLengthDetails.getConsentLength();

          var emissionDailyAverage = switch (consentLengthType) {
            case SHORT_TERM -> consentEmissionFigureService.getShortTermEmissionDailyAverage(applicationVersion);
            case ANNUAL -> consentEmissionFigureService.getAnnualEmissionDailyAverage(applicationVersion);
            default -> throw new IllegalStateException("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
          };

          return ConsentDataForm.fromEmissionApplication(
              proposedConsentStartDate,
              proposedConsentEndDate,
              emissionDailyAverage
          );
        });
  }

  public ConsentDataView getConsentDataView(
      Application application,
      ConsentData consentData,
      ConsentLengthType consentLengthType
  ) {
    return switch (application.getType()) {
      case PRODUCTION -> switch (consentLengthType) {
        case SHORT_TERM, ANNUAL ->
            getConsentDataViewForShortTermOrAnnualProductionApplication(consentData);
        case LONG_TERM -> getConsentDataViewForLongTermProductionApplication(application, consentData);
      };
      case FLARE, VENT -> getConsentDataViewForEmissionApplication(consentData);
    };
  }

  ConsentDataView getConsentDataViewForShortTermOrAnnualProductionApplication(ConsentData consentData) {
    return ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData);
  }

  ConsentDataView getConsentDataViewForLongTermProductionApplication(Application application, ConsentData consentData) {
    var consentDataLongTermProductionFiguresViews =
        consentDataLongTermProductionFiguresService.getConsentDataLongTermProductionFiguresViews(application);

    return ConsentDataView.fromLongTermProductionApplication(consentData, consentDataLongTermProductionFiguresViews);
  }

  ConsentDataView getConsentDataViewForEmissionApplication(ConsentData consentData) {
    return ConsentDataView.fromEmissionApplication(consentData);
  }
}

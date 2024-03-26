package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataForm;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Service
public class ConsentDataLongTermProductionFiguresService {

  private final ConsentDataLongTermProductionFiguresRepository consentDataLongTermProductionFiguresRepository;

  ConsentDataLongTermProductionFiguresService(
      ConsentDataLongTermProductionFiguresRepository consentDataLongTermProductionFiguresRepository) {
    this.consentDataLongTermProductionFiguresRepository = consentDataLongTermProductionFiguresRepository;
  }

  public List<ConsentDataLongTermProductionFigures> getConsentDataLongTermProductionFiguresList(Application application) {
    return consentDataLongTermProductionFiguresRepository.findAllByApplication(application);
  }

  public Map<String, ConsentProductionFiguresView> getConsentDataLongTermProductionFiguresViews(Application application) {
    return getConsentDataLongTermProductionFiguresList(application)
        .stream()
        .sorted(Comparator.comparing(ConsentDataLongTermProductionFigures::getYear))
        .collect(
            StreamUtils.toLinkedHashMap(
                consentDataLongTermProductionFigures -> consentDataLongTermProductionFigures.getYear().toString(),
                ConsentProductionFiguresView::fromConsentDataLongTermProductionFigures
            )
        );
  }

  @Transactional
  public void saveConsentDataLongTermProductionFigures(Application application, ConsentDataForm form) {
    consentDataLongTermProductionFiguresRepository.deleteAllByApplication(application);

    var consentDataLongTermProductionFiguresList = form.getLongTermConsentProductionFiguresInputs().entrySet()
        .stream()
        .map(entry -> newConsentDataLongTermProductionFigures(application, Integer.parseInt(entry.getKey()), entry.getValue()))
        .toList();

    consentDataLongTermProductionFiguresRepository.saveAll(consentDataLongTermProductionFiguresList);
  }

  @Transactional
  public void deleteConsentDataLongTermProductionFigures(Application application) {
    consentDataLongTermProductionFiguresRepository.deleteAllByApplication(application);
  }

  private ConsentDataLongTermProductionFigures newConsentDataLongTermProductionFigures(
      Application application,
      int year,
      ConsentProductionFiguresInput consentProductionFiguresInput
  ) {
    var consentDataLongTermProductionFigures = new ConsentDataLongTermProductionFigures();

    consentDataLongTermProductionFigures.setApplication(application);
    consentDataLongTermProductionFigures.setYear(year);

    var consentProductionFiguresDto = consentProductionFiguresInput.getAsDtoOrThrow();
    consentDataLongTermProductionFigures.setMinOil(consentProductionFiguresDto.minOil());
    consentDataLongTermProductionFigures.setMaxOil(consentProductionFiguresDto.maxOil());
    consentDataLongTermProductionFigures.setMinGas(consentProductionFiguresDto.minGas());
    consentDataLongTermProductionFigures.setMaxGas(consentProductionFiguresDto.maxGas());

    return consentDataLongTermProductionFigures;
  }
}

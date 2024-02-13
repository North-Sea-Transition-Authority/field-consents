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
public class ConsentProductionLongTermFiguresService {

  private final ConsentProductionLongTermFiguresRepository consentProductionLongTermFiguresRepository;

  ConsentProductionLongTermFiguresService(ConsentProductionLongTermFiguresRepository consentProductionLongTermFiguresRepository) {
    this.consentProductionLongTermFiguresRepository = consentProductionLongTermFiguresRepository;
  }

  public List<ConsentProductionLongTermFigures> getConsentProductionLongTermFiguresList(Application application) {
    return consentProductionLongTermFiguresRepository.findAllByApplication(application);
  }

  public Map<String, ConsentProductionFiguresView> getConsentProductionLongTermFiguresViews(Application application) {
    return getConsentProductionLongTermFiguresList(application)
        .stream()
        .sorted(Comparator.comparing(ConsentProductionLongTermFigures::getYear))
        .collect(
            StreamUtils.toLinkedHashMap(
                consentProductionLongTermFigures -> consentProductionLongTermFigures.getYear().toString(),
                ConsentProductionFiguresView::fromConsentProductionLongTermFigures
            )
        );
  }

  @Transactional
  public void saveConsentProductionLongTermFigures(Application application, ConsentDataForm form) {
    consentProductionLongTermFiguresRepository.deleteAllByApplication(application);

    var consentProductionLongTermFiguresList = form.getLongTermConsentProductionFiguresInputs().entrySet()
        .stream()
        .map(entry -> newConsentProductionLongTermFigures(application, Integer.parseInt(entry.getKey()), entry.getValue()))
        .toList();

    consentProductionLongTermFiguresRepository.saveAll(consentProductionLongTermFiguresList);
  }

  private ConsentProductionLongTermFigures newConsentProductionLongTermFigures(
      Application application,
      int year,
      ConsentProductionFiguresInput consentProductionFiguresInput
  ) {
    var consentProductionLongTermFigures = new ConsentProductionLongTermFigures();

    consentProductionLongTermFigures.setApplication(application);
    consentProductionLongTermFigures.setYear(year);

    var consentProductionFiguresDto = consentProductionFiguresInput.getAsDtoOrThrow();
    consentProductionLongTermFigures.setMinOil(consentProductionFiguresDto.minOil());
    consentProductionLongTermFigures.setMaxOil(consentProductionFiguresDto.maxOil());
    consentProductionLongTermFigures.setMinGas(consentProductionFiguresDto.minGas());
    consentProductionLongTermFigures.setMaxGas(consentProductionFiguresDto.maxGas());

    return consentProductionLongTermFigures;
  }
}

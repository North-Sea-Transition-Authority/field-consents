package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Service
public class ConsentDataLongTermEmissionFiguresService {

  private final ConsentDataLongTermEmissionFiguresRepository consentDataLongTermEmissionFiguresRepository;

  ConsentDataLongTermEmissionFiguresService(
      ConsentDataLongTermEmissionFiguresRepository consentDataLongTermEmissionFiguresRepository
  ) {
    this.consentDataLongTermEmissionFiguresRepository = consentDataLongTermEmissionFiguresRepository;
  }

  public List<ConsentDataLongTermEmissionFigures> getConsentDataLongTermEmissionFiguresList(Application application) {
    return consentDataLongTermEmissionFiguresRepository.findAllByApplication(application);
  }

  public Map<String, String> getConsentDataLongTermEmissionFiguresViews(Application application) {
    return getConsentDataLongTermEmissionFiguresList(application)
        .stream()
        .sorted(Comparator.comparing(ConsentDataLongTermEmissionFigures::getYear))
        .collect(
            StreamUtils.toLinkedHashMap(
                consentDataLongTermEmissionFigures -> consentDataLongTermEmissionFigures.getYear().toString(),
                consentDataLongTermEmissionFigures ->
                    bigDecimalToFormattedString(consentDataLongTermEmissionFigures.getDailyAverage())
            )
        );
  }
}

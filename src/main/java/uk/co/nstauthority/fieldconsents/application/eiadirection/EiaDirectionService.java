package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@Service
public class EiaDirectionService {

  static final String LOOKUP_SAT_REF_PURPOSE = "Lookup satRef prior to saving or displaying eia direction information";

  private final EiaDirectionRepository eiaDirectionRepository;

  private final PetsApplicationService petsApplicationService;

  @Autowired
  EiaDirectionService(EiaDirectionRepository eiaDirectionRepository,
                      PetsApplicationService petsApplicationService) {
    this.eiaDirectionRepository = eiaDirectionRepository;
    this.petsApplicationService = petsApplicationService;
  }

  public Optional<EiaDirection> findEiaDirection(ApplicationVersion applicationVersion) {
    return eiaDirectionRepository.findByApplicationVersion(applicationVersion);
  }

  EiaDirectionForm getEiaDirectionForm(ApplicationVersion applicationVersion) {
    return findEiaDirection(applicationVersion)
        .map(EiaDirectionForm::from)
        .orElseGet(EiaDirectionForm::new);
  }

  @Transactional
  public void saveEiaDirection(ApplicationVersion applicationVersion,
                               EiaDirectionForm eiaDirectionForm) {
    eiaDirectionRepository.deleteByApplicationVersion(applicationVersion);
    var satRef = getSatRef(eiaDirectionForm.getSatId());
    eiaDirectionRepository.save(EiaDirection.from(applicationVersion, eiaDirectionForm, satRef));
  }

  private String getSatRef(Integer satId) {
    return satId != null
        ? petsApplicationService.getPetsApplicationById(satId, LOOKUP_SAT_REF_PURPOSE).satRef()
        : null;
  }

  public SummaryDataView getEiaDirectionSummaryDataView(ApplicationVersion applicationVersion) {
    var eiaDirectionOptional = findEiaDirection(applicationVersion);
    List<SummaryKeyValue> summaryKeyValues = new ArrayList<>();
    var haveSubmittedEiaDirectionPrompt = "Have you submitted an EIA screening direction?";

    if (eiaDirectionOptional.isEmpty()) {
      summaryKeyValues.add(SummaryKeyValue.fromKeyNoValue(haveSubmittedEiaDirectionPrompt));
      return new SummaryDataView(summaryKeyValues);
    }

    var eiaDirection = eiaDirectionOptional.get();

    summaryKeyValues.add(SummaryKeyValue.fromBoolean(haveSubmittedEiaDirectionPrompt,
        eiaDirection.getHaveSubmittedEiaDirection()));

    if (Boolean.TRUE.equals(eiaDirection.getHaveSubmittedEiaDirection())) {
      summaryKeyValues.add(SummaryKeyValue.from("EIA screening direction reference",
          getSatRef(eiaDirection.getSatId())));

      return new SummaryDataView(summaryKeyValues);
    }

    summaryKeyValues.add(SummaryKeyValue.fromBoolean("Do you have an EIA screening direction that still needs to be submitted?",
        eiaDirection.getHaveEiaDirectionToSubmit()));

    if (Boolean.TRUE.equals(eiaDirection.getHaveEiaDirectionToSubmit())) {
      summaryKeyValues.add(SummaryKeyValue.fromLocalDate("What is the latest date this will be submitted?",
          eiaDirection.getLatestDateToBeSubmitted()));

      return new SummaryDataView(summaryKeyValues);
    }

    summaryKeyValues.add(SummaryKeyValue.from("Explain why you don’t intend to submit an EIA screening direction",
        eiaDirection.getWhyNoEiaDirection()));

    return new SummaryDataView(summaryKeyValues);
  }
}

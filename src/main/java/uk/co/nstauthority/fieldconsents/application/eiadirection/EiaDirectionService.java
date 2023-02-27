package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;

@Service
public class EiaDirectionService {

  static final String LOOKUP_SAT_REF_PURPOSE = "Lookup satRef prior to saving eia direction information";

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
    String satRef = eiaDirectionForm.getSatId() != null
        ? petsApplicationService.getPetsApplicationById(eiaDirectionForm.getSatId(), LOOKUP_SAT_REF_PURPOSE).satRef()
        : null;
    eiaDirectionRepository.save(EiaDirection.from(applicationVersion, eiaDirectionForm, satRef));
  }
}

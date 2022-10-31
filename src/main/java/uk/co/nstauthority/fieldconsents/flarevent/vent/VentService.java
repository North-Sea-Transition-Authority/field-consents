package uk.co.nstauthority.fieldconsents.flarevent.vent;

import java.util.Comparator;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
class VentService {

  private final VentRepository ventRepository;

  @Autowired
  VentService(VentRepository ventRepository) {
    this.ventRepository = ventRepository;
  }

  public List<Vent> getVentsForApplicationVersion(ApplicationVersion applicationVersion) {
    return ventRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion);
  }

  public boolean ventsExistForApplicationVersion(ApplicationVersion applicationVersion) {
    return !getVentsForApplicationVersion(applicationVersion).isEmpty();
  }

  public Vent getVentOrError(ApplicationVersion applicationVersion, Integer ventNo) {
    return ventRepository.findByApplicationVersionAndVentNo(applicationVersion, ventNo)
        .orElseThrow(() ->
            new EntityNotFoundException("Vent with application_version_id %s and vent_no %s not found"
                .formatted(applicationVersion.getId(), ventNo))
        );
  }

  @Transactional
  public void deleteVent(Vent vent) {
    ventRepository.delete(vent);
  }

  @Transactional
  public void saveNewVent(ApplicationVersion applicationVersion, VentForm ventForm) {
    // find the next vent no to use
    Integer nextVentNo = getVentsForApplicationVersion(applicationVersion).stream()
        .max(Comparator.comparing(Vent::getVentNo))
        .map(vent -> vent.getVentNo() + 1).orElse(1);

    ventRepository.save(Vent.newFromForm(applicationVersion, nextVentNo, ventForm));
  }

  @Transactional
  public void updateVentFromForm(Vent vent, VentForm ventForm) {
    vent.updateFromForm(ventForm);
    ventRepository.save(vent);
  }

}

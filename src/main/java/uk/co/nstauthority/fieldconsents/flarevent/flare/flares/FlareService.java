package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import java.util.Comparator;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
public class FlareService {

  private final FlareRepository flareRepository;

  @Autowired
  FlareService(FlareRepository flareRepository) {
    this.flareRepository = flareRepository;
  }

  public List<Flare> getFlaresForApplicationVersion(ApplicationVersion applicationVersion) {
    return flareRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion);
  }

  public boolean flaresExistForApplicationVersion(ApplicationVersion applicationVersion) {
    return !getFlaresForApplicationVersion(applicationVersion).isEmpty();
  }

  public Flare getFlareOrError(ApplicationVersion applicationVersion, Integer flareNo) {
    return flareRepository.findByApplicationVersionAndFlareNo(applicationVersion, flareNo)
        .orElseThrow(() ->
            new EntityNotFoundException("Flare with application_version_id %s and flare_no %s not found"
                .formatted(applicationVersion.getId(), flareNo))
        );
  }

  @Transactional
  public void deleteFlare(Flare flare) {
    flareRepository.delete(flare);
  }

  @Transactional
  public void saveNewFlare(ApplicationVersion applicationVersion, FlareForm flareForm) {
    // find the next flare no to use
    Integer nextFlareNo = getFlaresForApplicationVersion(applicationVersion).stream()
        .max(Comparator.comparing(Flare::getFlareNo))
        .map(flare -> flare.getFlareNo() + 1).orElse(1);

    flareRepository.save(Flare.newFromForm(applicationVersion, nextFlareNo, flareForm));
  }

  @Transactional
  public void updateFlareFromForm(Flare flare, FlareForm flareForm) {
    flare.updateFromForm(flareForm);
    flareRepository.save(flare);
  }

}

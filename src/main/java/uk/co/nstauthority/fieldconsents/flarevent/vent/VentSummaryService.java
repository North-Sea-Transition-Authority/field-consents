package uk.co.nstauthority.fieldconsents.flarevent.vent;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
class VentSummaryService {

  private VentService ventService;

  @Autowired
  VentSummaryService(VentService ventService) {
    this.ventService = ventService;
  }

  List<VentView> getSummaryViews(ApplicationVersion applicationVersion) {
    return createVentViews(ventService.getVentsForApplicationVersion(applicationVersion));
  }

  private List<VentView> createVentViews(List<Vent> vents) {
    return IntStream.range(0, vents.size())
        .mapToObj(index -> VentView.from(vents.get(index), index + 1))
        .toList();
  }
}

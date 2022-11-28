package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
class FlareSummaryService {

  private FlareService flareService;

  @Autowired
  FlareSummaryService(FlareService flareService) {
    this.flareService = flareService;
  }

  List<FlareView> getSummaryViews(ApplicationVersion applicationVersion) {
    return createFlareViews(flareService.getFlaresForApplicationVersion(applicationVersion));
  }

  private List<FlareView> createFlareViews(List<Flare> flares) {
    return IntStream.range(0, flares.size())
        .mapToObj(index -> FlareView.from(flares.get(index), index + 1))
        .toList();
  }
}

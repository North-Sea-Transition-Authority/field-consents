package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@Service
public class FlareSummaryService {

  private final FlareService flareService;

  @Autowired
  FlareSummaryService(FlareService flareService) {
    this.flareService = flareService;
  }

  List<FlareView> getFlareViews(ApplicationVersion applicationVersion) {
    return createFlareViews(flareService.getFlaresForApplicationVersion(applicationVersion));
  }

  private List<FlareView> createFlareViews(List<Flare> flares) {
    return IntStream.range(0, flares.size())
        .mapToObj(index -> FlareView.from(flares.get(index), index + 1))
        .toList();
  }

  public List<SummaryGroup> getSummariesForFlares(ApplicationVersion applicationVersion) {
    var flareViews = getFlareViews(applicationVersion);

    if (flareViews.isEmpty()) {
      return SummaryGroup.emptySummaryGroupList();
    }

    return flareViews
        .stream()
        .map(flareView -> SummaryGroup.simpleSummaryGroupWithHeading(
            "Flare " + flareView.getDisplayOrder(),
            List.of(
                SummaryKeyValue.from("Flare type", flareView.getFlareType()),
                SummaryKeyValue.from("Description", flareView.getDescription()),
                SummaryKeyValue.from("Metered", flareView.getMeteredFlag()),
                SummaryKeyValue.from("Comments", flareView.getComments())
            )
        )).toList();
  }
}

package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

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

  public List<SummaryCard> getSummariesForFlares(ApplicationVersion applicationVersion) {
    var flareViews = getFlareViews(applicationVersion);

    if (flareViews.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    return flareViews
        .stream()
        .map(flareView -> SummaryCard.simpleSummaryCardWithHeading(
            "Flare " + flareView.getDisplayOrder(),
            SummaryDataView
                .newWithKeyValue("Flare type", flareView.getFlareType())
                .addKeyValue("Description", flareView.getDescription())
                .addKeyValue("Metered", flareView.getMeteredFlag())
                .addKeyValue("Comments", flareView.getComments())
            )
        ).toList();
  }
}

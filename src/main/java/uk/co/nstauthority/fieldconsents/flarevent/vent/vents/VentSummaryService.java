package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@Service
public class VentSummaryService {

  private final VentService ventService;

  @Autowired
  VentSummaryService(VentService ventService) {
    this.ventService = ventService;
  }

  List<VentView> getVentViews(ApplicationVersion applicationVersion) {
    return createVentViews(ventService.getVentsForApplicationVersion(applicationVersion));
  }

  private List<VentView> createVentViews(List<Vent> vents) {
    return IntStream.range(0, vents.size())
        .mapToObj(index -> VentView.from(vents.get(index), index + 1))
        .toList();
  }

  public List<SummaryCard> getSummariesForVents(ApplicationVersion applicationVersion) {
    var ventViews = getVentViews(applicationVersion);

    if (ventViews.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    return ventViews
        .stream()
        .map(ventView -> SummaryCard.simpleSummaryCardWithHeading(
            "Vent " + ventView.getDisplayOrder(),
            List.of(
                SummaryKeyValue.from("Vent type", ventView.getVentType()),
                SummaryKeyValue.from("Description", ventView.getDescription()),
                SummaryKeyValue.from("Metered", ventView.getMeteredFlag()),
                SummaryKeyValue.from("Comments", ventView.getComments())
            )
        )).toList();
  }
}

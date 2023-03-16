package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareTestUtil.flares;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class FlareSummaryServiceTest {

  @Mock
  private FlareService flareService;

  private FlareSummaryService flareSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    flareSummaryService = new FlareSummaryService(flareService);
    applicationVersion = FlareTestUtil.flareAppVersion;
  }

  @Test
  void getFlareViews_noFlares() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    var flareViews = flareSummaryService.getFlareViews(applicationVersion);

    assertThat(flareViews).isEmpty();
  }

  @Test
  void getFlareViews_manyFlares() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(flares);

    var flareViews = flareSummaryService.getFlareViews(applicationVersion);
    String expectedUrlBase = "/applications/" + applicationVersion.getApplication().getId() + "/flares/";
    String expectedUrlTailDelete = "/delete";

    assertThat(flareViews)
        .extracting(
            FlareView::getDisplayOrder,
            FlareView::getFlareNo,
            FlareView::getEditUrl,
            FlareView::getDeleteUrl,
            FlareView::getFlareType,
            FlareView::getDescription,
            FlareView::getMeteredFlag,
            FlareView::getComments
        )
        .containsExactly(
            tuple(1,
                flares.get(0).getFlareNo(),
                expectedUrlBase + flares.get(0).getFlareNo(),
                expectedUrlBase + flares.get(0).getFlareNo() + expectedUrlTailDelete,
                flares.get(0).getFlareType().getDisplayName(),
                flares.get(0).getDescription(),
                flares.get(0).getMeteredFlag() ? "Yes" : "No",
                flares.get(0).getComments()),
            tuple(2,
                flares.get(1).getFlareNo(),
                expectedUrlBase + flares.get(1).getFlareNo(),
                expectedUrlBase + flares.get(1).getFlareNo() + expectedUrlTailDelete,
                flares.get(1).getFlareType().getDisplayName(),
                flares.get(1).getDescription(),
                flares.get(1).getMeteredFlag() ? "Yes" : "No",
                flares.get(1).getComments()),
            tuple(3,
                flares.get(2).getFlareNo(),
                expectedUrlBase + flares.get(2).getFlareNo(),
                expectedUrlBase + flares.get(2).getFlareNo() + expectedUrlTailDelete,
                flares.get(2).getFlareType().getDisplayName(),
                flares.get(2).getDescription(),
                flares.get(2).getMeteredFlag() ? "Yes" : "No",
                flares.get(2).getComments()),
            tuple(4,
                flares.get(3).getFlareNo(),
                expectedUrlBase + flares.get(3).getFlareNo(),
                expectedUrlBase + flares.get(3).getFlareNo() + expectedUrlTailDelete,
                flares.get(3).getFlareType().getDisplayName(),
                flares.get(3).getDescription(),
                flares.get(3).getMeteredFlag() ? "Yes" : "No",
                flares.get(3).getComments())
        );
  }

  @Test
  void getFlaresSummaryGroups_noFlaresExist() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(Collections.emptyList());

    assertThat(flareSummaryService.getSummariesForFlares(applicationVersion))
        .isEqualTo(SummaryGroup.emptySummaryGroupList());
  }

  @Test
  void getFlaresSummaryGroups() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(flares);
    var flareViews =
        List.of(FlareView.from(flares.get(0), 1), FlareView.from(flares.get(1), 2),
            FlareView.from(flares.get(2), 3), FlareView.from(flares.get(3), 4)
        );

    var summaryGroups = flareSummaryService.getSummariesForFlares(applicationVersion);

    var flarePrompt = "Flare ";
    var flareTypePrompt = "Flare type";
    var descPrompt = "Description";
    var meteredPrompt = "Metered";
    var commentsPrompt = "Comments";

    assertThat(summaryGroups)
        .isEqualTo(
            List.of(
                SummaryGroup.simpleSummaryGroupWithHeading(flarePrompt + flareViews.get(0).getDisplayOrder(),
                    List.of(
                        SummaryKeyValue.from(flareTypePrompt, flareViews.get(0).getFlareType()),
                        SummaryKeyValue.from(descPrompt, flareViews.get(0).getDescription()),
                        SummaryKeyValue.from(meteredPrompt, flareViews.get(0).getMeteredFlag()),
                        SummaryKeyValue.from(commentsPrompt, flareViews.get(0).getComments())
                    )),
                SummaryGroup.simpleSummaryGroupWithHeading(flarePrompt + flareViews.get(1).getDisplayOrder(),
                    List.of(
                        SummaryKeyValue.from(flareTypePrompt, flareViews.get(1).getFlareType()),
                        SummaryKeyValue.from(descPrompt, flareViews.get(1).getDescription()),
                        SummaryKeyValue.from(meteredPrompt, flareViews.get(1).getMeteredFlag()),
                        SummaryKeyValue.from(commentsPrompt, flareViews.get(1).getComments())
                    )),
                SummaryGroup.simpleSummaryGroupWithHeading(flarePrompt + flareViews.get(2).getDisplayOrder(),
                    List.of(
                        SummaryKeyValue.from(flareTypePrompt, flareViews.get(2).getFlareType()),
                        SummaryKeyValue.from(descPrompt, flareViews.get(2).getDescription()),
                        SummaryKeyValue.from(meteredPrompt, flareViews.get(2).getMeteredFlag()),
                        SummaryKeyValue.from(commentsPrompt, flareViews.get(2).getComments())
                    )),
                SummaryGroup.simpleSummaryGroupWithHeading(flarePrompt + flareViews.get(3).getDisplayOrder(),
                    List.of(
                        SummaryKeyValue.from(flareTypePrompt, flareViews.get(3).getFlareType()),
                        SummaryKeyValue.from(descPrompt, flareViews.get(3).getDescription()),
                        SummaryKeyValue.from(meteredPrompt, flareViews.get(3).getMeteredFlag()),
                        SummaryKeyValue.from(commentsPrompt, flareViews.get(3).getComments())
                    ))
            )
        );
  }
}

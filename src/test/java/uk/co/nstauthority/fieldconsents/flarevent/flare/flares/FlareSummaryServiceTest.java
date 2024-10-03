package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareTestUtil.flares;
import static uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareTestUtil.flaresWithNulls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
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
  void getSummariesForFlares_noFlaresExist() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(Collections.emptyList());

    assertThat(flareSummaryService.getSummariesForFlares(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCardList());
  }

  @Test
  void getSummariesForFlares() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(flares);
    var flareViews =
        List.of(FlareView.from(flares.get(0), 1), FlareView.from(flares.get(1), 2),
            FlareView.from(flares.get(2), 3), FlareView.from(flares.get(3), 4)
        );

    var summaryCards = flareSummaryService.getSummariesForFlares(applicationVersion);

    var flarePrompt = "Flare ";
    var flareTypePrompt = "Flare type";
    var descPrompt = "Description";
    var meteredPrompt = "Metered";
    var commentsPrompt = "Comments";

    assertThat(summaryCards)
        .isEqualTo(
            List.of(
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(0).getDisplayOrder(),
                    new SummaryDataView(List.of(
                        new SummaryKeyValue(flareTypePrompt, flareViews.get(0).getFlareType()),
                        new SummaryKeyValue(descPrompt, flareViews.get(0).getDescription()),
                        new SummaryKeyValue(meteredPrompt, flareViews.get(0).getMeteredFlag()),
                        new SummaryKeyValue(commentsPrompt, flareViews.get(0).getComments())
                    ))
                ),
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(1).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(flareTypePrompt, flareViews.get(1).getFlareType())
                        .addKeyValue(descPrompt, flareViews.get(1).getDescription())
                        .addKeyValue(meteredPrompt, flareViews.get(1).getMeteredFlag())
                        .addKeyValue(commentsPrompt, flareViews.get(1).getComments())
                ),
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(2).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(flareTypePrompt, flareViews.get(2).getFlareType())
                        .addKeyValue(descPrompt, flareViews.get(2).getDescription())
                        .addKeyValue(meteredPrompt, flareViews.get(2).getMeteredFlag())
                        .addKeyValue(commentsPrompt, flareViews.get(2).getComments())
                ),
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(3).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(flareTypePrompt, flareViews.get(3).getFlareType())
                        .addKeyValue(descPrompt, flareViews.get(3).getDescription())
                        .addKeyValue(meteredPrompt, flareViews.get(3).getMeteredFlag())
                        .addKeyValue(commentsPrompt, flareViews.get(3).getComments())
                )
            )
        );
  }

  @Test
  void getSummariesForFlares_migratedFlares() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(flaresWithNulls);
    var flareViews =
        List.of(FlareView.from(flaresWithNulls.get(0), 1),
            FlareView.from(flaresWithNulls.get(1), 2),
            FlareView.from(flaresWithNulls.get(2), 3),
            FlareView.from(flaresWithNulls.get(3), 4)
        );

    var summaryCards = flareSummaryService.getSummariesForFlares(applicationVersion);

    var flarePrompt = "Flare ";
    var flareTypePrompt = "Flare type";
    var descPrompt = "Description";
    var meteredPrompt = "Metered";
    var commentsPrompt = "Comments";

    assertThat(summaryCards)
        .isEqualTo(
            List.of(
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(0).getDisplayOrder(),
                    new SummaryDataView(List.of(
                        new SummaryKeyValue(flareTypePrompt, flareViews.get(0).getFlareType()),
                        new SummaryKeyValue(descPrompt, flareViews.get(0).getDescription()),
                        new SummaryKeyValue(meteredPrompt, flareViews.get(0).getMeteredFlag()),
                        new SummaryKeyValue(commentsPrompt, flareViews.get(0).getComments())
                    ))
                ),
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(1).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(flareTypePrompt, flareViews.get(1).getFlareType())
                        .addKeyValue(descPrompt, "")
                        .addKeyValue(meteredPrompt, flareViews.get(1).getMeteredFlag())
                        .addKeyValue(commentsPrompt, flareViews.get(1).getComments())
                ),
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(2).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(flareTypePrompt, flareViews.get(2).getFlareType())
                        .addKeyValue(descPrompt, flareViews.get(2).getDescription())
                        .addKeyValue(meteredPrompt, flareViews.get(2).getMeteredFlag())
                        .addKeyValue(commentsPrompt, "")
                ),
                SummaryCard.simpleSummaryCardWithHeading(flarePrompt + flareViews.get(3).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(flareTypePrompt, flareViews.get(3).getFlareType())
                        .addKeyValue(descPrompt, flareViews.get(3).getDescription())
                        .addKeyValue(meteredPrompt, flareViews.get(3).getMeteredFlag())
                        .addKeyValue(commentsPrompt, flareViews.get(3).getComments())
                )
            )
        );
  }
}

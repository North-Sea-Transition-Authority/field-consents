package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

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
  void getSummaryViews_noFlares() {
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    var flareViews = flareSummaryService.getSummaryViews(applicationVersion);

    assertThat(flareViews).isEmpty();
  }

  @Test
  void getSummaryViews_manyFlares() {
    var flares = FlareTestUtil.flares;
    when(flareService.getFlaresForApplicationVersion(applicationVersion)).thenReturn(flares);

    var flareViews = flareSummaryService.getSummaryViews(applicationVersion);
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

}

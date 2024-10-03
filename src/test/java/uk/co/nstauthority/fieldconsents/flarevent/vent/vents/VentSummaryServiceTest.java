package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentTestUtil.vents;
import static uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentTestUtil.ventsWithNulls;

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
class VentSummaryServiceTest {

  @Mock
  private VentService ventService;

  private VentSummaryService ventSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    ventSummaryService = new VentSummaryService(ventService);
    applicationVersion = VentTestUtil.ventAppVersion;
  }

  @Test
  void getSummaryViews_noVents() {
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    var ventViews = ventSummaryService.getVentViews(applicationVersion);

    assertThat(ventViews).isEmpty();
  }

  @Test
  void getSummaryViews_manyVents() {
    var vents = VentTestUtil.vents;
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(vents);

    var ventViews = ventSummaryService.getVentViews(applicationVersion);
    String expectedUrlBase = "/applications/" + applicationVersion.getApplication().getId() + "/vents/";
    String expectedUrlTailDelete = "/delete";

    assertThat(ventViews)
        .extracting(
            VentView::getDisplayOrder,
            VentView::getVentNo,
            VentView::getEditUrl,
            VentView::getDeleteUrl,
            VentView::getVentType,
            VentView::getDescription,
            VentView::getMeteredFlag,
            VentView::getComments
        )
        .containsExactly(
            tuple(1,
                vents.get(0).getVentNo(),
                expectedUrlBase + vents.get(0).getVentNo(),
                expectedUrlBase + vents.get(0).getVentNo() + expectedUrlTailDelete,
                vents.get(0).getVentType().getDisplayName(),
                vents.get(0).getDescription(),
                vents.get(0).getMeteredFlag() ? "Yes" : "No",
                vents.get(0).getComments()),
            tuple(2,
                vents.get(1).getVentNo(),
                expectedUrlBase + vents.get(1).getVentNo(),
                expectedUrlBase + vents.get(1).getVentNo() + expectedUrlTailDelete,
                vents.get(1).getVentType().getDisplayName(),
                vents.get(1).getDescription(),
                vents.get(1).getMeteredFlag() ? "Yes" : "No",
                vents.get(1).getComments()),
            tuple(3,
                vents.get(2).getVentNo(),
                expectedUrlBase + vents.get(2).getVentNo(),
                expectedUrlBase + vents.get(2).getVentNo() + expectedUrlTailDelete,
                vents.get(2).getVentType().getDisplayName(),
                vents.get(2).getDescription(),
                vents.get(2).getMeteredFlag() ? "Yes" : "No",
                vents.get(2).getComments())
        );
  }

  @Test
  void getSummariesForVents_noVentsExist() {
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(Collections.emptyList());

    assertThat(ventSummaryService.getSummariesForVents(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCardList());
  }

  @Test
  void getSummariesForVents() {
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(vents);
    var ventViews =
        List.of(VentView.from(vents.get(0), 1), VentView.from(vents.get(1), 2),
            VentView.from(vents.get(2), 3)
        );

    var summaryCards = ventSummaryService.getSummariesForVents(applicationVersion);

    var ventPrompt = "Vent ";
    var ventTypePrompt = "Vent type";
    var descPrompt = "Description";
    var meteredPrompt = "Metered";
    var commentsPrompt = "Comments";

    assertThat(summaryCards)
        .isEqualTo(
            List.of(
                SummaryCard.simpleSummaryCardWithHeading(ventPrompt + ventViews.get(0).getDisplayOrder(),
                    new SummaryDataView(List.of(
                        new SummaryKeyValue(ventTypePrompt, ventViews.get(0).getVentType()),
                        new SummaryKeyValue(descPrompt, ventViews.get(0).getDescription()),
                        new SummaryKeyValue(meteredPrompt, ventViews.get(0).getMeteredFlag()),
                        new SummaryKeyValue(commentsPrompt, ventViews.get(0).getComments())
                    ))
                ),
                SummaryCard.simpleSummaryCardWithHeading(ventPrompt + ventViews.get(1).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(ventTypePrompt, ventViews.get(1).getVentType())
                        .addKeyValue(descPrompt, ventViews.get(1).getDescription())
                        .addKeyValue(meteredPrompt, ventViews.get(1).getMeteredFlag())
                        .addKeyValue(commentsPrompt, ventViews.get(1).getComments())
                ),
                SummaryCard.simpleSummaryCardWithHeading(ventPrompt + ventViews.get(2).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(ventTypePrompt, ventViews.get(2).getVentType())
                        .addKeyValue(descPrompt, ventViews.get(2).getDescription())
                        .addKeyValue(meteredPrompt, ventViews.get(2).getMeteredFlag())
                        .addKeyValue(commentsPrompt, ventViews.get(2).getComments())
                )
            )
        );
  }

  @Test
  void getSummariesForVents_migratedVents() {
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(ventsWithNulls);
    var ventViews =
        List.of(VentView.from(ventsWithNulls.get(0), 1), VentView.from(ventsWithNulls.get(1), 2),
            VentView.from(ventsWithNulls.get(2), 3)
        );

    var summaryCards = ventSummaryService.getSummariesForVents(applicationVersion);

    var ventPrompt = "Vent ";
    var ventTypePrompt = "Vent type";
    var descPrompt = "Description";
    var meteredPrompt = "Metered";
    var commentsPrompt = "Comments";

    assertThat(summaryCards)
        .isEqualTo(
            List.of(
                SummaryCard.simpleSummaryCardWithHeading(ventPrompt + ventViews.get(0).getDisplayOrder(),
                    new SummaryDataView(List.of(
                        new SummaryKeyValue(ventTypePrompt, ventViews.get(0).getVentType()),
                        new SummaryKeyValue(descPrompt, ventViews.get(0).getDescription()),
                        new SummaryKeyValue(meteredPrompt, ventViews.get(0).getMeteredFlag()),
                        new SummaryKeyValue(commentsPrompt, ventViews.get(0).getComments())
                    ))
                ),
                SummaryCard.simpleSummaryCardWithHeading(ventPrompt + ventViews.get(1).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(ventTypePrompt, ventViews.get(1).getVentType())
                        .addKeyValue(descPrompt, "")
                        .addKeyValue(meteredPrompt, ventViews.get(1).getMeteredFlag())
                        .addKeyValue(commentsPrompt, ventViews.get(1).getComments())
                ),
                SummaryCard.simpleSummaryCardWithHeading(ventPrompt + ventViews.get(2).getDisplayOrder(),
                    SummaryDataView
                        .newWithKeyValue(ventTypePrompt, ventViews.get(2).getVentType())
                        .addKeyValue(descPrompt, ventViews.get(2).getDescription())
                        .addKeyValue(meteredPrompt, ventViews.get(2).getMeteredFlag())
                        .addKeyValue(commentsPrompt, "")
                )
            )
        );
  }
}

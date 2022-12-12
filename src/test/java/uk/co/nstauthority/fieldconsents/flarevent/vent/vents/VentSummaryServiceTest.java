package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

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

    var ventViews = ventSummaryService.getSummaryViews(applicationVersion);

    assertThat(ventViews).isEmpty();
  }

  @Test
  void getSummaryViews_manyVents() {
    var vents = VentTestUtil.vents;
    when(ventService.getVentsForApplicationVersion(applicationVersion)).thenReturn(vents);

    var ventViews = ventSummaryService.getSummaryViews(applicationVersion);
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

}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

class FurtherInformationViewTest {

  @Test
  void builder_withRequestedAtTimestamp() {
    var instant = Instant.now();
    assertThat(FurtherInformationView.newBuilder()
        .withRequestedAtTimestamp(instant)
        .build())
        .extracting(FurtherInformationView::requestedAtTimestamp)
        .isEqualTo(DateUtils.format(instant, DateUtils.DATE_TIME));
  }

  @Test
  void builder_withRequestedAtTimestamp_null() {
    assertThat(FurtherInformationView.newBuilder()
        .withRequestedAtTimestamp(null)
        .build())
        .extracting(FurtherInformationView::requestedAtTimestamp)
        .isNull();
  }

  @Test
  void builder_withRequestedByUser() {
    var user = mock(EnergyPortalUserDto.class);
    when(user.displayName()).thenReturn("user");
    assertThat(FurtherInformationView.newBuilder()
        .withRequestedByUser(user)
        .build())
        .extracting(FurtherInformationView::requestedByUser)
        .isEqualTo("user");
  }

  @Test
  void builder_withRequestedByUser_null() {
    assertThat(FurtherInformationView.newBuilder()
        .withRequestedByUser(null)
        .build())
        .extracting(FurtherInformationView::requestedByUser)
        .isNull();
  }

  @Test
  void builder_withRespondedAtTimestamp() {
    var instant = Instant.now();
    assertThat(FurtherInformationView.newBuilder()
        .withRespondedAtTimestamp(instant)
        .build())
        .extracting(FurtherInformationView::respondedAtTimestamp)
        .isEqualTo(DateUtils.format(instant, DateUtils.DATE_TIME));
  }

  @Test
  void builder_withRespondedAtTimestamp_null() {
    assertThat(FurtherInformationView.newBuilder()
        .withRespondedAtTimestamp(null)
        .build())
        .extracting(FurtherInformationView::respondedAtTimestamp)
        .isNull();
  }

  @Test
  void builder_withRespondedByUser() {
    var user = mock(EnergyPortalUserDto.class);
    when(user.displayName()).thenReturn("user");
    assertThat(FurtherInformationView.newBuilder()
        .withRespondedByUser(user)
        .build())
        .extracting(FurtherInformationView::respondedByUser)
        .isEqualTo("user");
  }

  @Test
  void builder_withRespondedByUser_null() {
    assertThat(FurtherInformationView.newBuilder()
        .withRespondedByUser(null)
        .build())
        .extracting(FurtherInformationView::respondedByUser)
        .isNull();
  }

}

package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@ExtendWith(MockitoExtension.class)
class FurtherInformationViewTest {

  private static final Instant REQUESTED_AT_TIMESTAMP = Instant.now();
  private static final String REQUESTED_BY_USER = "Requesting user";
  private static final String REQUEST_TEXT = "Request text";
  private static final Instant RESPONDED_AT_TIMESTAMP = Instant.now();
  private static final String RESPONDED_BY_USER = "Responding user";
  private static final String RESPONSE_TEXT = "Response text";

  @ParameterizedTest
  @MethodSource("toSummaryCardWithHeading_arguments")
  void toSummaryCardWithHeading(String heading, FurtherInformationView furtherInformationView, SummaryDataView summaryDataView) {
    assertThat(furtherInformationView.toSummaryCardWithHeading(heading))
        .extracting(SummaryCard::displayName, SummaryCard::summaryCardType, SummaryCard::summaryData)
        .containsExactly(heading, SummaryCardType.SIMPLE_SUMMARY, summaryDataView);
  }

  private static Stream<Arguments> toSummaryCardWithHeading_arguments() {
    var requestingUser = mock(EnergyPortalUserDto.class);
    when(requestingUser.displayName()).thenReturn(REQUESTED_BY_USER);

    var respondingUser = mock(EnergyPortalUserDto.class);
    when(respondingUser.displayName()).thenReturn(RESPONDED_BY_USER);

    return Stream.of(
        arguments(
            "Open further information request",
            FurtherInformationView.newBuilder()
                .withStatus(FurtherInformationStatus.OPEN)
                .withRequestedAtTimestamp(REQUESTED_AT_TIMESTAMP)
                .withRequestedByUser(requestingUser)
                .withRequestText(REQUEST_TEXT)
                .build(),
            SummaryDataView
                .newWithKeyValue("Requested on", DateUtils.format(REQUESTED_AT_TIMESTAMP, DateUtils.DATE_TIME))
                .addKeyValue("Requested by", requestingUser.displayName())
                .addKeyValue("Request text", REQUEST_TEXT)
        ),
        arguments(
            "Closed further information request",
            FurtherInformationView.newBuilder()
                .withStatus(FurtherInformationStatus.CLOSED)
                .withRequestedAtTimestamp(REQUESTED_AT_TIMESTAMP)
                .withRequestedByUser(requestingUser)
                .withRequestText(REQUEST_TEXT)
                .withRespondedAtTimestamp(RESPONDED_AT_TIMESTAMP)
                .withRespondedByUser(respondingUser)
                .withResponseText(RESPONSE_TEXT)
                .build(),
            SummaryDataView
                .newWithKeyValue("Requested on", DateUtils.format(REQUESTED_AT_TIMESTAMP, DateUtils.DATE_TIME))
                .addKeyValue("Requested by", requestingUser.displayName())
                .addKeyValue("Request text", REQUEST_TEXT)
                .addKeyValue("Responded on", DateUtils.format(RESPONDED_AT_TIMESTAMP, DateUtils.DATE_TIME))
                .addKeyValue("Responded by", respondingUser.displayName())
                .addKeyValue("Response text", RESPONSE_TEXT)
        )
    );
  }

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

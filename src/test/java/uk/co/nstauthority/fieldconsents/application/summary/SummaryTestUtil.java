package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

public class SummaryTestUtil {

  public static final int CONSENT_DETAILS_DISPLAY_ORDER = 10;

  public static final int PRODUCTION_INFORMATION_DISPLAY_ORDER = 20;

  public static final int FLARE_INFORMATION_DISPLAY_ORDER = 20;

  public static final int VENT_INFORMATION_DISPLAY_ORDER = 20;

  public static final int ADDITIONAL_INFORMATION_DISPLAY_ORDER = 30;

  public static final List<SummaryKeyValue> keyValues =
      List.of(new SummaryKeyValue("k1", "v1"), new SummaryKeyValue("k2", "v2"));

  public static SummaryCard simpleSummaryCard = SummaryCard.simpleSummaryCard(new SummaryDataView(keyValues));

  public static SummarySection getConsentDetailsSummarySection(List<SummaryItem> summaryItems) {
    return new SummarySection(CONSENT_DETAILS_DISPLAY_ORDER, summaryItems);
  }

  public static SummarySection getProductionInformationSummarySection(List<SummaryItem> summaryItems) {
    return new SummarySection(PRODUCTION_INFORMATION_DISPLAY_ORDER, summaryItems);
  }

  public static SummarySection getAdditionalInformationSummarySection(List<SummaryItem> summaryItems) {
    return new SummarySection(ADDITIONAL_INFORMATION_DISPLAY_ORDER, summaryItems);
  }

  public static void assertSummarySection(SummarySection summarySection, int displayOrder) {
    assertThat(summarySection.displayOrder()).isEqualTo(displayOrder);
  }

  public static void assertSummaryItem(SummaryItem summaryItem,
                                       String displayName,
                                       int expectedSummaryCardCount) {
    assertThat(summaryItem.displayName()).isEqualTo(displayName);
    assertThat(summaryItem.summaryCards()).hasSize(expectedSummaryCardCount);
  }

  public static void assertSummaryCard(SummaryCard summaryCard,
                                       String displayName,
                                       SummaryCardType summaryCardType,
                                       Class<?> clazz) {
    assertThat(summaryCard.displayName()).isEqualTo(displayName);
    assertThat(summaryCard.summaryCardType()).isEqualTo(summaryCardType);
    assertThat(summaryCard.summaryData()).isInstanceOf(clazz);
  }

  public static void assertEmptySummaryCard(SummaryCard summaryCard) {
    assertThat(summaryCard.displayName()).isNull();
    assertThat(summaryCard.summaryCardType()).isEqualTo(SummaryCardType.EMPTY_SUMMARY);
    assertThat(summaryCard.summaryData()).isNull();
  }
}
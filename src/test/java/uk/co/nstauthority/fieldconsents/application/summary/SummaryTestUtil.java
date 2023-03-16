package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroupType;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

public class SummaryTestUtil {

  public static final int CONSENT_DETAILS_DISPLAY_ORDER = 10;

  public static final int PRODUCTION_INFORMATION_DISPLAY_ORDER = 20;

  public static final int FLARE_INFORMATION_DISPLAY_ORDER = 20;

  public static final int ADDITIONAL_INFORMATION_DISPLAY_ORDER = 30;

  public static final List<SummaryKeyValue> keyValues =
      List.of(new SummaryKeyValue("k1", "v1"), new SummaryKeyValue("k2", "v2"));

  public static SummaryGroup simpleSummaryGroup = SummaryGroup.simpleSummaryGroup(keyValues);

  public static final List<SummaryGroup> simpleSummaryGroups =
      List.of(
          SummaryGroup.simpleSummaryGroupWithHeading("g1", keyValues),
          SummaryGroup.simpleSummaryGroupWithHeading("g2", keyValues)
      );

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
                                       int expectedSummaryGroupCount) {
    assertThat(summaryItem.displayName()).isEqualTo(displayName);
    assertThat(summaryItem.summaryGroups()).hasSize(expectedSummaryGroupCount);
  }

  public static void assertSummaryGroup(SummaryGroup summaryGroup,
                                        String displayName,
                                        SummaryGroupType summaryGroupType,
                                        Class<?> clazz) {
    assertThat(summaryGroup.displayName()).isEqualTo(displayName);
    assertThat(summaryGroup.summaryGroupType()).isEqualTo(summaryGroupType);
    assertThat(summaryGroup.summaryData()).isInstanceOf(clazz);
  }

  public static void assertEmptySummaryGroup(SummaryGroup summaryGroup) {
    assertThat(summaryGroup.displayName()).isNull();
    assertThat(summaryGroup.summaryGroupType()).isEqualTo(SummaryGroupType.EMPTY_SUMMARY);
    assertThat(summaryGroup.summaryData()).isNull();
  }
}
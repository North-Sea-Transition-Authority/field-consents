package uk.co.nstauthority.fieldconsents.application.summary.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.FLARE_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertEmptySummaryGroup;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryGroup;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.simpleSummaryGroups;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroupType;

@ExtendWith(MockitoExtension.class)
class FlareInformationSummarySectionServiceTest {

  private static final String FLARES_ITEM = "Flares";

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private FlareAnnualService flareAnnualService;

  @Mock
  private FlareShortTermService flareShortTermService;

  @Mock
  private FlareSummaryService flareSummaryService;

  @InjectMocks FlareInformationSummarySectionService flareInformationSummarySectionService;

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"PRODUCTION", "VENT"})
  void getSummarySection_nonFlare(ApplicationType applicationType) {
    var nonFlareAppVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);

    assertThat(flareInformationSummarySectionService.getSummarySection(nonFlareAppVersion))
        .isNotPresent();
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(flareInformationSummarySectionService.getSummarySection(applicationVersion))
        .isNotPresent();
  }

  @ParameterizedTest
  @MethodSource("getSummaryGroupList")
  void getSummarySection_shortTerm_empty(List<SummaryGroup> summaryGroups) {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(flareSummaryService.getSummariesForFlares(applicationVersion))
        .thenReturn(summaryGroups);

    var summarySectionOptional = flareInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, FLARE_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    if (SummaryGroupType.EMPTY_SUMMARY.equals(summaryGroups.get(0).summaryGroupType())) {
      assertSummaryItem(summaryItems.get(0), FLARES_ITEM, 1);
      assertEmptySummaryGroup(summaryItems.get(0).summaryGroups().get(0));
    } else {
      assertSummaryItem(summaryItems.get(0), FLARES_ITEM, 2);
      assertSummaryGroup(summaryItems.get(0).summaryGroups().get(0), summaryGroups.get(0).displayName(),
          SummaryGroupType.SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryGroup(summaryItems.get(0).summaryGroups().get(1), summaryGroups.get(1).displayName(),
          SummaryGroupType.SIMPLE_SUMMARY, SummaryDataView.class);
    }
  }

  private static Stream<Arguments> getSummaryGroupList() {
    return Stream.of(
        Arguments.of(SummaryGroup.emptySummaryGroupList()),
        Arguments.of(simpleSummaryGroups)
    );
  }
}
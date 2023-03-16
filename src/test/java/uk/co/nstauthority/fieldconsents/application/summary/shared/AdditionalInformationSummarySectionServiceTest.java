package uk.co.nstauthority.fieldconsents.application.summary.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.ADDITIONAL_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertEmptySummaryGroup;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryGroup;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.simpleSummaryGroup;
import static uk.co.nstauthority.fieldconsents.application.summary.shared.AdditionalInformationSummarySectionService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.summary.SummaryGroupType.SIMPLE_SUMMARY;

import java.util.Optional;
import java.util.stream.Stream;
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
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroupType;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

@ExtendWith(MockitoExtension.class)
class AdditionalInformationSummarySectionServiceTest {

  private static final String EIA_SCREENING_DIRECTION_ITEM = "EIA screening direction";

  private static final String SUPPORTING_INFORMATION_ITEM = "Supporting information";

  @Mock
  private SupportingInformationService supportingInformationService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private EiaDirectionService eiaDirectionService;

  @InjectMocks
  private AdditionalInformationSummarySectionService additionalInformationSummarySectionService;

  @ParameterizedTest
  @MethodSource("getAppTypeSummaryGroup")
  void getSummarySection_offshore(ApplicationType applicationType, SummaryGroup summaryGroup) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);
    when(eiaDirectionService.getEiaDirectionSummaryGroup(applicationVersion))
        .thenReturn(summaryGroup);
    when(supportingInformationService.getSupportingInformationSummaryGroup(applicationVersion))
        .thenReturn(summaryGroup);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field1Json);

    var summarySectionOptional = additionalInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(2);
    assertSummaryItem(summaryItems.get(0), EIA_SCREENING_DIRECTION_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), SUPPORTING_INFORMATION_ITEM, 1);

    if (SummaryGroupType.EMPTY_SUMMARY.equals(summaryGroup.summaryGroupType())) {
      assertEmptySummaryGroup(summaryItems.get(0).summaryGroups().get(0));
      assertEmptySummaryGroup(summaryItems.get(1).summaryGroups().get(0));
    } else {
      assertSummaryGroup(summaryItems.get(0).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryGroup(summaryItems.get(1).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
    }
  }

  private static Stream<Arguments> getAppTypeSummaryGroup() {
    return Stream.of(
        Arguments.of(ApplicationType.PRODUCTION, SummaryGroup.emptySummaryGroup()),
        Arguments.of(ApplicationType.FLARE, SummaryGroup.emptySummaryGroup()),
        Arguments.of(ApplicationType.VENT, SummaryGroup.emptySummaryGroup()),
        Arguments.of(ApplicationType.PRODUCTION, simpleSummaryGroup),
        Arguments.of(ApplicationType.FLARE, simpleSummaryGroup),
        Arguments.of(ApplicationType.VENT, simpleSummaryGroup)
    );
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection_onshore(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);
    when(supportingInformationService.getSupportingInformationSummaryGroup(applicationVersion))
        .thenReturn(simpleSummaryGroup);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset2.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field2Json);

    var summarySectionOptional = additionalInformationSummarySectionService.getSummarySection(applicationVersion);

    assertSectionAndSingleItem(summarySectionOptional);
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection_unknownShore(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);
    when(supportingInformationService.getSupportingInformationSummaryGroup(applicationVersion))
        .thenReturn(simpleSummaryGroup);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset3);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset3.getFieldId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field3Json);

    var summarySectionOptional = additionalInformationSummarySectionService.getSummarySection(applicationVersion);

    assertSectionAndSingleItem(summarySectionOptional);
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection_terminal(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);
    when(supportingInformationService.getSupportingInformationSummaryGroup(applicationVersion))
        .thenReturn(simpleSummaryGroup);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.terminalAsset1);

    var summarySectionOptional = additionalInformationSummarySectionService.getSummarySection(applicationVersion);

    assertSectionAndSingleItem(summarySectionOptional);
  }

  private void assertSectionAndSingleItem(Optional<SummarySection> summarySectionOptional) {
    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), SUPPORTING_INFORMATION_ITEM, 1);
    assertSummaryGroup(summaryItems.get(0).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
  }
}
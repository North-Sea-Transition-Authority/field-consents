package uk.co.nstauthority.fieldconsents.application.summary.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.PRODUCTION_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.summary.SummaryItemType.PRODUCTION_ANNUAL;
import static uk.co.nstauthority.fieldconsents.summary.SummaryItemType.PRODUCTION_LONG_TERM;
import static uk.co.nstauthority.fieldconsents.summary.SummaryItemType.PRODUCTION_SHORT_TERM;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.production.ProductionView;
import uk.co.nstauthority.fieldconsents.production.ProductionViewTestUtil;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;

@ExtendWith(MockitoExtension.class)
class ProductionInformationSummarySectionServiceTest {

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ShortTermProductionService shortTermProductionService;

  @Mock
  private AnnualProductionService annualProductionService;

  @Mock
  private LongTermProductionService longTermProductionService;

  @InjectMocks
  private ProductionInformationSummarySectionService productionInformationSummarySectionService;

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void getSummarySection_nonProduction(ApplicationType applicationType) {
    var nonProductionAppVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);

    assertThat(productionInformationSummarySectionService.getSummarySection(nonProductionAppVersion))
        .isNotPresent();
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(productionInformationSummarySectionService.getSummarySection(applicationVersion))
        .isNotPresent();
  }

  @Test
  void getSummarySection_shortTerm() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(shortTermProductionService.getProductionShortTermView(applicationVersion))
        .thenReturn(ProductionViewTestUtil.emptyShortTerm());

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), ConsentLengthType.SHORT_TERM.getDisplayName(), PRODUCTION_SHORT_TERM, ProductionView.class);
  }

  @Test
  void getSummarySection_annual() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion)));
    when(annualProductionService.getProductionAnnualView(applicationVersion))
        .thenReturn(ProductionViewTestUtil.emptyAnnual());

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), ConsentLengthType.ANNUAL.getDisplayName(), PRODUCTION_ANNUAL, ProductionView.class);
  }

  @Test
  void getSummarySection_longTerm() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion)));
    when(longTermProductionService.getProductionLongTermView(applicationVersion))
        .thenReturn(ProductionViewTestUtil.emptyLongTerm());

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), ConsentLengthType.LONG_TERM.getDisplayName(), PRODUCTION_LONG_TERM, ProductionView.class);
  }
}
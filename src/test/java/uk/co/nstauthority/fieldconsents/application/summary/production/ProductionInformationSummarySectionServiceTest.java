package uk.co.nstauthority.fieldconsents.application.summary.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.PRODUCTION_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertEmptySummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.charts.ProductionChartData;
import uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryChart;
import uk.co.nstauthority.fieldconsents.summary.SummaryChartType;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummaryTestUtil;

@ExtendWith(MockitoExtension.class)
class ProductionInformationSummarySectionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private final ProductionChartData oilProductionChartData = new ProductionChartData(
      "Oil production chart",
      List.of("Jan", "Feb", "Mar"),
      "month",
      "volume KSCM/month",
      List.of(new ProductionChartData.Series(
              "Oil Production",
              "highcharts-color-red",
              "columnrange",
              List.of(
                  new ProductionChartData.DataPoint(0, 10, 20, "highcharts-color-red"),
                  new ProductionChartData.DataPoint(1, 15, 25, "highcharts-color-red"),
                  new ProductionChartData.DataPoint(2, 20, 25, "highcharts-color-red")
              )
          )
      )
  );
  @Mock
  private ConsentLengthService consentLengthService;
  @Mock
  private ProductionSummaryService productionSummaryService;
  @InjectMocks
  private ProductionInformationSummarySectionService productionInformationSummarySectionService;
  private final ProductionChartData gasProductionChartData = new ProductionChartData(
      "Gas production chart",
      List.of("Apr", "May", "Jun"),
      "month",
      "volume KSCM/month",
      List.of(new ProductionChartData.Series(
              "Gas Production",
              "highcharts-color-dark-blue",
              "columnrange",
              List.of(
                  new ProductionChartData.DataPoint(0, 14, 18, "highcharts-color-green"),
                  new ProductionChartData.DataPoint(1, 21, 24, "highcharts-color-green"),
                  new ProductionChartData.DataPoint(2, 28, 30, "highcharts-color-green")
              )
          )
      )
  );
  private final List<SummaryCard> productionSummaryChartCards = List.of(
      SummaryCard.floatingBarChartSummaryCard(oilProductionChartData),
      SummaryCard.floatingBarChartSummaryCard(gasProductionChartData)
  );
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String oilProductionChartDataAsJson =
      objectMapper.writeValueAsString(oilProductionChartData);
  private final String gasProductionChartDataAsJson =
      objectMapper.writeValueAsString(gasProductionChartData);
  ApplicationVersion applicationVersion;

  ProductionInformationSummarySectionServiceTest() throws JsonProcessingException {
  }

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void getSummarySection_nonProduction(ApplicationType applicationType) {
    var nonProductionAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    assertThat(productionInformationSummarySectionService.getSummarySection(nonProductionAppVersion, USER))
        .isNotPresent();

    verifyNoInteractions(consentLengthService);
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(productionInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .isNotPresent();
  }

  @Test
  void getSummarySection_emptySummaryCard_shortTerm() {
    var summaryCard = SummaryCard.emptySummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(productionSummaryService.getShortTermConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(productionSummaryService.getProductionConsentChartSummaryCards(applicationVersion))
        .thenReturn(Collections.emptyList());

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    assertSummaryItem(summaryItems.getFirst(), ConsentLengthType.SHORT_TERM.getDisplayName(), 1);
    assertEmptySummaryCard(summaryItems.getFirst().summaryCards().getFirst());
  }

  @Test
  void getSummarySection_emptySummaryCardAndChart_shortTerm() {
    var summaryCard = SummaryCard.emptySummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(productionSummaryService.getShortTermConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(productionSummaryService.getProductionConsentChartSummaryCards(applicationVersion))
        .thenReturn(productionSummaryChartCards);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    assertThat(summaryItems.getFirst())
        .extracting(
            SummaryItem::displayName,
            summaryItem -> summaryItem.summaryCards().size()
        )
        .contains(
            ConsentLengthType.SHORT_TERM.getDisplayName(),
            3);
    assertThat(summaryItems.getFirst().summaryCards())
        .extracting(
            SummaryCard::displayName,
            SummaryCard::summaryCardType,
            SummaryCard::summaryData)
        .contains(
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    oilProductionChartDataAsJson
                )
            ),
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    gasProductionChartDataAsJson
                )
            ),
            new Tuple(
                summaryCard.displayName(),
                summaryCard.summaryCardType(),
                summaryCard.summaryData()
            )
        );
  }

  @Test
  void getSummarySection_tableSummaryCard_shortTerm() {
    var summaryCard = SummaryTestUtil.getTableSummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(productionSummaryService.getShortTermConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(productionSummaryService.getProductionConsentChartSummaryCards(applicationVersion))
        .thenReturn(Collections.emptyList());

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    assertSummaryItem(summaryItems.getFirst(), ConsentLengthType.SHORT_TERM.getDisplayName(), 1);
    assertSummaryCard(summaryItems.getFirst().summaryCards().getFirst(), summaryCard.displayName(),
        summaryCard.summaryCardType(), summaryCard.summaryData().getClass());
  }

  @Test
  void getSummarySection_tableSummaryCardAndChart_shortTerm() {
    var summaryCard = SummaryTestUtil.getTableSummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(productionSummaryService.getShortTermConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(productionSummaryService.getProductionConsentChartSummaryCards(applicationVersion))
        .thenReturn(productionSummaryChartCards);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    assertThat(summaryItems.getFirst())
        .extracting(
            SummaryItem::displayName,
            summaryItem -> summaryItem.summaryCards().size()
        )
        .contains(
            ConsentLengthType.SHORT_TERM.getDisplayName(),
            3);
    assertThat(summaryItems.getFirst().summaryCards())
        .extracting(
            SummaryCard::displayName,
            SummaryCard::summaryCardType,
            SummaryCard::summaryData)
        .contains(
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    oilProductionChartDataAsJson
                )
            ),
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    oilProductionChartDataAsJson
                )
            ),
            new Tuple(
                summaryCard.displayName(),
                summaryCard.summaryCardType(),
                summaryCard.summaryData()
            )
        );
  }

  @Test
  void getSummarySection_emptySummaryCard_annual() {
    var summaryCard = SummaryCard.emptySummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion)));
    when(productionSummaryService.getAnnualConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.getFirst(), ConsentLengthType.ANNUAL.getDisplayName(), 1);
    assertEmptySummaryCard(summaryItems.getFirst().summaryCards().getFirst());
  }

  @Test
  void getSummarySection_emptySummaryCardAndChart_annual() {
    var summaryCard = SummaryCard.emptySummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion)));
    when(productionSummaryService.getAnnualConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(productionSummaryService.getProductionConsentChartSummaryCards(applicationVersion))
        .thenReturn(productionSummaryChartCards);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    assertThat(summaryItems.getFirst())
        .extracting(
            SummaryItem::displayName,
            summaryItem -> summaryItem.summaryCards().size()
        )
        .contains(
            ConsentLengthType.ANNUAL.getDisplayName(),
            3);
    assertThat(summaryItems.getFirst().summaryCards())
        .extracting(
            SummaryCard::displayName,
            SummaryCard::summaryCardType,
            SummaryCard::summaryData)
        .contains(
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    oilProductionChartDataAsJson
                )
            ),
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    gasProductionChartDataAsJson
                )
            ),
            new Tuple(
                summaryCard.displayName(),
                summaryCard.summaryCardType(),
                summaryCard.summaryData()
            )
        );
  }

  @Test
  void getSummarySection_tableSummaryCard_annual() {
    var summaryCard = SummaryTestUtil.getTableSummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion)));
    when(productionSummaryService.getAnnualConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.getFirst(), ConsentLengthType.ANNUAL.getDisplayName(), 1);
    assertSummaryCard(summaryItems.getFirst().summaryCards().getFirst(), summaryCard.displayName(),
        summaryCard.summaryCardType(), summaryCard.summaryData().getClass());
  }

  @Test
  void getSummarySection_tableSummaryCardAndChart_annual() {
    var summaryCard = SummaryTestUtil.getTableSummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion)));
    when(productionSummaryService.getAnnualConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(productionSummaryService.getProductionConsentChartSummaryCards(applicationVersion))
        .thenReturn(productionSummaryChartCards);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    assertThat(summaryItems.getFirst())
        .extracting(
            SummaryItem::displayName,
            summaryItem -> summaryItem.summaryCards().size()
        )
        .contains(
            ConsentLengthType.ANNUAL.getDisplayName(),
            3);
    assertThat(summaryItems.getFirst().summaryCards())
        .extracting(
            SummaryCard::displayName,
            SummaryCard::summaryCardType,
            SummaryCard::summaryData)
        .contains(
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    oilProductionChartDataAsJson
                )
            ),
            new Tuple(
                null,
                SummaryCardType.CHART_SUMMARY,
                new SummaryChart(
                    SummaryChartType.FLOATING_BAR_CHART,
                    gasProductionChartDataAsJson
                )
            ),
            new Tuple(
                summaryCard.displayName(),
                summaryCard.summaryCardType(),
                summaryCard.summaryData()
            )
        );
  }

  @Test
  void getSummarySection_emptySummaryCard_longTerm() {
    var summaryCard = SummaryCard.emptySummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion)));
    when(productionSummaryService.getLongTermConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), ConsentLengthType.LONG_TERM.getDisplayName(), 1);
    if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
      assertEmptySummaryCard(summaryItems.get(0).summaryCards().get(0));
    } else {
      assertSummaryCard(summaryItems.get(0).summaryCards().get(0), summaryCard.displayName(),
          summaryCard.summaryCardType(), summaryCard.summaryData().getClass());
    }
  }

  @Test
  void getSummarySection_tableSummaryCard_longTerm() {
    var summaryCard = SummaryTestUtil.getTableSummaryCard();
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion)));
    when(productionSummaryService.getLongTermConsentSummaryCard(applicationVersion))
        .thenReturn(summaryCard);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), ConsentLengthType.LONG_TERM.getDisplayName(), 1);
    if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
      assertEmptySummaryCard(summaryItems.get(0).summaryCards().get(0));
    } else {
      assertSummaryCard(summaryItems.get(0).summaryCards().get(0), summaryCard.displayName(),
          summaryCard.summaryCardType(), summaryCard.summaryData().getClass());
    }
  }
}

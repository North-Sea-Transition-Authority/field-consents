package uk.co.nstauthority.fieldconsents.application.summary.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.summary.SummaryItemType.SIMPLE_SUMMARY;

import java.util.Collections;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@ExtendWith(MockitoExtension.class)
class ConsentDetailsSummarySectionServiceTest {

  private static final String APPLICATION_DETAILS_ITEM = "Application details";

  private static final String CONSENT_DURATION_ITEM = "Consent duration";

  private static final String GAS_INJECTION_ITEM = "Gas injection";

  @Mock
  private ApplicationContextService applicationContextService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private GasInjectionService gasInjectionService;

  @InjectMocks
  private ConsentDetailsSummarySectionService consentDetailsSummarySectionService;

  SummaryDataView summaryDataView = new SummaryDataView(Collections.emptyList());

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);
    when(applicationContextService.getApplicationContextSummaryDataView(applicationVersion))
        .thenReturn(summaryDataView);
    when(consentLengthService.getConsentLengthSummaryDataView(applicationVersion))
        .thenReturn(summaryDataView);
    if (ApplicationType.PRODUCTION.equals(applicationType)) {
      when(gasInjectionService.getGasInjectionSummaryDataView(applicationVersion))
          .thenReturn(summaryDataView);
    }

    var summarySectionOptional = consentDetailsSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, CONSENT_DETAILS_DISPLAY_ORDER);
    var summaryItems = summarySection.summaryItems();

    assertSummaryItem(summaryItems.get(0), APPLICATION_DETAILS_ITEM, SIMPLE_SUMMARY, SummaryDataView.class);
    assertSummaryItem(summaryItems.get(1), CONSENT_DURATION_ITEM, SIMPLE_SUMMARY, SummaryDataView.class);

    if (ApplicationType.PRODUCTION.equals(applicationType)) {
      assertThat(summaryItems).hasSize(3);
      assertSummaryItem(summaryItems.get(2), GAS_INJECTION_ITEM, SIMPLE_SUMMARY, SummaryDataView.class);
    } else {
      assertThat(summaryItems).hasSize(2);
    }
  }
}
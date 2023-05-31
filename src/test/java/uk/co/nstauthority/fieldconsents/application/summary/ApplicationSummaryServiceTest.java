package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.ADDITIONAL_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.PRODUCTION_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.getAdditionalInformationSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.getConsentDetailsSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.getProductionInformationSummarySection;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.summary.production.ProductionInformationSummarySectionService;
import uk.co.nstauthority.fieldconsents.application.summary.shared.AdditionalInformationSummarySectionService;
import uk.co.nstauthority.fieldconsents.application.summary.shared.ConsentDetailsSummarySectionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class ApplicationSummaryServiceTest {

  @Mock
  private ConsentDetailsSummarySectionService consentDetailsSummarySectionService;

  @Mock
  private AdditionalInformationSummarySectionService additionalInformationSummarySectionService;

  @Mock
  private ProductionInformationSummarySectionService productionInformationSummarySectionService;

  private ApplicationSummaryService applicationSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationSummaryService = new ApplicationSummaryService(List.of(
        additionalInformationSummarySectionService,
        productionInformationSummarySectionService,
        consentDetailsSummarySectionService
    ));
  }

  @Test
  void getSummarySections_production() {
    when(consentDetailsSummarySectionService.getSummarySection(applicationVersion))
        .thenReturn(Optional.of(getConsentDetailsSummarySection(null)));
    when(productionInformationSummarySectionService.getSummarySection(applicationVersion))
        .thenReturn(Optional.of(getProductionInformationSummarySection(null)));
    when(additionalInformationSummarySectionService.getSummarySection(applicationVersion))
        .thenReturn(Optional.of(getAdditionalInformationSummarySection(null)));

    List<SummarySection> summarySections = applicationSummaryService.getSummarySections(applicationVersion);

    assertThat(summarySections).hasSize(3);
    assertSummarySection(summarySections.get(0), CONSENT_DETAILS_DISPLAY_ORDER);
    assertSummarySection(summarySections.get(1), PRODUCTION_INFORMATION_DISPLAY_ORDER);
    assertSummarySection(summarySections.get(2), ADDITIONAL_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getApplicationSummaryModelAndView() {
    var viewName = "fcs/application/applicationSummary";
    var pageTitle = "Application summary";
    var backLinkUrl = "/work-area";

    var modelAndView = applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        viewName,
        pageTitle,
        backLinkUrl
    );

    assertThat(modelAndView.getModel())
        .containsEntry("pageTitle", pageTitle)
        .containsKey("summarySections")
        .containsEntry("accordionId", applicationVersion.getId())
        .containsEntry("wideSummaryDisplay", false)
        .containsEntry("backLinkUrl", ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null)));
  }
}

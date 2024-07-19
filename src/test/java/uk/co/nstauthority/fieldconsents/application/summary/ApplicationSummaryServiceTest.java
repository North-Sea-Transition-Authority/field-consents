package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.ADDITIONAL_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.PRODUCTION_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.getAdditionalInformationSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.getConsentDetailsSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.getProductionInformationSummarySection;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.summary.production.ProductionInformationSummarySectionService;
import uk.co.nstauthority.fieldconsents.application.summary.shared.AdditionalInformationSummarySectionService;
import uk.co.nstauthority.fieldconsents.application.summary.shared.ConsentDetailsSummarySectionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

@ExtendWith(MockitoExtension.class)
class ApplicationSummaryServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ConsentDetailsSummarySectionService consentDetailsSummarySectionService;

  @Mock
  private AdditionalInformationSummarySectionService additionalInformationSummarySectionService;

  @Mock
  private ProductionInformationSummarySectionService productionInformationSummarySectionService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  private ApplicationSummaryService applicationSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationSummaryService = new ApplicationSummaryService(List.of(
        additionalInformationSummarySectionService,
        productionInformationSummarySectionService,
        consentDetailsSummarySectionService
    ), applicationVersionService);
  }

  @Test
  void getSummarySections_production() {
    when(consentDetailsSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(getConsentDetailsSummarySection(null)));
    when(productionInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(getProductionInformationSummarySection(null)));
    when(additionalInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(getAdditionalInformationSummarySection(null)));

    List<SummarySection> summarySections = applicationSummaryService.getSummarySections(applicationVersion, null);

    assertThat(summarySections).hasSize(3);
    assertSummarySection(summarySections.get(0), CONSENT_DETAILS_DISPLAY_ORDER);
    assertSummarySection(summarySections.get(1), PRODUCTION_INFORMATION_DISPLAY_ORDER);
    assertSummarySection(summarySections.get(2), ADDITIONAL_INFORMATION_DISPLAY_ORDER);
  }

  @Test
  void getApplicationSummaryModelAndView() {
    var viewName = "fcs/application/applicationSummary";
    var pageTitle = "Application summary";

    var modelAndView = applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        viewName,
        pageTitle,
        USER);

    assertThat(modelAndView.getModel())
        .containsEntry("pageTitle", pageTitle)
        .containsKey("summarySections")
        .containsEntry("accordionId", applicationVersion.getId())
        .containsEntry("wideSummaryDisplay", false);
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void addSummarySectionsToModelAndView(ApplicationType applicationType) {
    // it doesn't actually matter what the sections here are...
    var consentDetailSection = getConsentDetailsSummarySection(null);
    when(consentDetailsSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(consentDetailSection));

    var productionDetailSection = getProductionInformationSummarySection(null);
    when(productionInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(productionDetailSection));

    var additionalDetailSection = getAdditionalInformationSummarySection(null);
    when(additionalInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(additionalDetailSection));

    var newApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var modelAndView = new ModelAndView();

    applicationSummaryService.addSummarySectionsToModelAndView(newApplicationVersion, modelAndView, null);

    var wideSummaryDisplay = ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationType);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(Map.of(
            "summarySections", List.of(consentDetailSection, productionDetailSection, additionalDetailSection),
            "accordionId", newApplicationVersion.getId(),
            "wideSummaryDisplay", wideSummaryDisplay
        ));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void addSummarySectionsAndVersionOptionsToModelAndView_withOneSubmittedApplication(ApplicationType applicationType) {
    // it doesn't actually matter what the sections here are...
    var consentDetailSection = getConsentDetailsSummarySection(null);
    when(consentDetailsSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(consentDetailSection));

    var productionDetailSection = getProductionInformationSummarySection(null);
    when(productionInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(productionDetailSection));

    var additionalDetailSection = getAdditionalInformationSummarySection(null);
    when(additionalInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(additionalDetailSection));

    var latestApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    latestApplicationVersion.setVersion(2);

    var modelAndView = new ModelAndView();

    applicationSummaryService.addSummarySectionsAndVersionOptionsToModelAndView(latestApplicationVersion, modelAndView, null);

    var wideSummaryDisplay = ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationType);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(Map.of(
            "summarySections", List.of(consentDetailSection, productionDetailSection, additionalDetailSection),
            "accordionId", latestApplicationVersion.getId(),
            "wideSummaryDisplay", wideSummaryDisplay
        ));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void addSummarySectionsAndVersionOptionsToModelAndView_withMultipleSubmittedApplications(ApplicationType applicationType) {
    // it doesn't actually matter what the sections here are...
    var consentDetailSection = getConsentDetailsSummarySection(null);
    when(consentDetailsSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(consentDetailSection));

    var productionDetailSection = getProductionInformationSummarySection(null);
    when(productionInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(productionDetailSection));

    var additionalDetailSection = getAdditionalInformationSummarySection(null);
    when(additionalInformationSummarySectionService.getSummarySection(applicationVersion, null))
        .thenReturn(Optional.of(additionalDetailSection));

    var latestApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(applicationType);
    latestApplicationVersion.setVersion(2);

    when(applicationVersionService.getAllNonDeletedApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(List.of(applicationVersion, latestApplicationVersion));

    var modelAndView = new ModelAndView();

    applicationSummaryService.addSummarySectionsAndVersionOptionsToModelAndView(latestApplicationVersion, modelAndView, null);

    var expectedApplicationVersions = Map.of(
        latestApplicationVersion.getVersion(), "Version %s: %s".formatted(latestApplicationVersion.getVersion(),
            DateUtils.format(latestApplicationVersion.getSubmittedDateTime(), DateUtils.SHORT_DATE)),
        applicationVersion.getVersion(), "Version %s: %s".formatted(applicationVersion.getVersion(),
            DateUtils.format(applicationVersion.getSubmittedDateTime(), DateUtils.SHORT_DATE))
    );

    var wideSummaryDisplay = ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationType);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(Map.of(
            "summarySections", List.of(consentDetailSection, productionDetailSection, additionalDetailSection),
            "accordionId", latestApplicationVersion.getId(),
            "wideSummaryDisplay", wideSummaryDisplay,
            "currentVersionNumber", latestApplicationVersion.getVersion(),
            "availableVersions", expectedApplicationVersions
        ));
  }

  @Test
  void getApplicationVersionDisplayText_whenInProgressApplicationVersion() {
    var inProgressApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    assertThat(applicationSummaryService.getApplicationVersionDisplayText(inProgressApplicationVersion))
        .isEqualTo("Version %s: %s".formatted(
            applicationVersion.getVersion(),
                inProgressApplicationVersion.getStatus().getDisplayName()));
  }

  @Test
  void getApplicationVersionDisplayText_whenNotInProgressApplicationVersion() {
    assertThat(applicationSummaryService.getApplicationVersionDisplayText(applicationVersion))
        .isEqualTo("Version %s: %s".formatted(
            applicationVersion.getVersion(),
            DateUtils.format(applicationVersion.getSubmittedDateTime(), DateUtils.SHORT_DATE)));
  }
}

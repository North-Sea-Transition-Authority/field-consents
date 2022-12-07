package uk.co.nstauthority.fieldconsents.flarevent.flare;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ShortTermUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualMonth;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;

@Service
class FlareMonthCleanupService implements ApplicationListener<ConsentLengthChangeEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlareMonthCleanupService.class);

  private final FlareAnnualMonthRepository flareAnnualMonthRepository;

  private final FlareAnnualService flareAnnualService;

  private final FlareShortTermMonthRepository flareShortTermMonthRepository;

  private final FlareShortTermService flareShortTermService;

  private final ApplicationVersionService applicationVersionService;

  private final ConsentLengthService consentLengthService;

  @Autowired
  FlareMonthCleanupService(FlareAnnualMonthRepository flareAnnualMonthRepository,
                           FlareAnnualService flareAnnualService,
                           FlareShortTermMonthRepository flareShortTermMonthRepository,
                           FlareShortTermService flareShortTermService,
                           ApplicationVersionService applicationVersionService,
                           ConsentLengthService consentLengthService) {
    this.flareAnnualMonthRepository = flareAnnualMonthRepository;
    this.flareAnnualService = flareAnnualService;
    this.flareShortTermMonthRepository = flareShortTermMonthRepository;
    this.flareShortTermService = flareShortTermService;
    this.applicationVersionService = applicationVersionService;
    this.consentLengthService = consentLengthService;
  }

  @Override
  public void onApplicationEvent(ConsentLengthChangeEvent event) {
    ApplicationVersion applicationVersion = applicationVersionService
        .getApplicationVersionById(event.getApplicationVersionId());
    ApplicationType applicationType = applicationVersion.getApplication().getType();

    if (applicationType.equals(ApplicationType.FLARE)) {
      ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

      ConsentLengthType consentLengthType = consentLengthDetails.getConsentLength();
      switch (consentLengthType) {
        case SHORT_TERM -> removeObsoleteDataWhenShortTerm(applicationVersion, consentLengthDetails);
        case ANNUAL -> removeObsoleteDataWhenAnnual(applicationVersion, consentLengthDetails);
        default -> throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
      }
    }
  }

  /**
   * Executed when the consent length is change to be annual. This deletes any existing short term data for
   * the given application version, and also deletes any old flare annual data if the annual consent year
   * has changed.
   *
   * @param applicationVersion the application version.
   * @param consentLengthDetails the new consent length details for the application version.
   */
  private void removeObsoleteDataWhenAnnual(ApplicationVersion applicationVersion,
                                            ConsentLengthDetails consentLengthDetails) {
    flareShortTermMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old flare data removed when consent length changed to annual for application version with id {}.",
        applicationVersion.getId());

    List<FlareAnnualMonth> existingFlareAnnualMonths = flareAnnualService.getFlareAnnualMonths(applicationVersion);

    if (!existingFlareAnnualMonths.isEmpty()) {
      int newAnnualConsentYear = consentLengthDetails.getAnnualConsentYear();

      for (FlareAnnualMonth flareAnnualMonth: existingFlareAnnualMonths) {
        if (flareAnnualMonth.getYear() != newAnnualConsentYear) {
          flareAnnualMonthRepository.delete(flareAnnualMonth);

          LOGGER.debug("Old flare annual data removed for month / year {} / {}.",
              flareAnnualMonth.getMonth(), flareAnnualMonth.getYear());
        }
      }
    }
  }

  /**
   * Executed when the consent length details have changed to be short term. This deletes any existing annual
   * data for the given application version, and also deletes stale flare short term data where the month term
   * is now not expected (based on the new consent length start and end dates).
   *
   * @param applicationVersion the application version.
   * @param consentLengthDetails the new consent length details for the application version.
   */
  private void removeObsoleteDataWhenShortTerm(ApplicationVersion applicationVersion,
                                               ConsentLengthDetails consentLengthDetails) {
    flareAnnualMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old flare data removed when consent length changed to short term for application version with id {}.",
        applicationVersion.getId());

    // get the existing saved flare short term months data
    List<FlareShortTermMonth> existingFlareShortTermMonths =
        flareShortTermService.getFlareShortTermMonths(applicationVersion);

    // create a set of expected start and end date pairs, i.e. for the new short term consent length details
    // we expect to have the following month data for each of these pairs
    Set<Pair<LocalDate, LocalDate>> expectedShortTermMonthTermsSet =
        new HashSet<>(ShortTermUtil.getExpectedMonthTerms(consentLengthDetails.getShortTermStartDate(),
            consentLengthDetails.getShortTermEndDate()));

    // loop over the existing short term flare data and delete it if it now isn't an expected month row
    for (FlareShortTermMonth flareShortTermMonth : existingFlareShortTermMonths) {
      if (!expectedShortTermMonthTermsSet.contains(Pair.of(flareShortTermMonth.getStartDate(),
          flareShortTermMonth.getEndDate()))) {
        flareShortTermMonthRepository.delete(flareShortTermMonth);

        LOGGER.debug("Old flare short term data removed for start date / end date {} / {}.",
            flareShortTermMonth.getStartDate(), flareShortTermMonth.getEndDate());
      }
    }
  }
}

package uk.co.nstauthority.fieldconsents.flarevent.vent;

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
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermMonthRepository;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;

@Service
class VentMonthCleanupService implements ApplicationListener<ConsentLengthChangeEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(VentMonthCleanupService.class);

  private final VentAnnualMonthRepository ventAnnualMonthRepository;

  private final VentAnnualService ventAnnualService;

  private final VentShortTermMonthRepository ventShortTermMonthRepository;

  private final VentShortTermService ventShortTermService;

  private final ApplicationVersionService applicationVersionService;

  private final ConsentLengthService consentLengthService;

  @Autowired
  VentMonthCleanupService(VentAnnualMonthRepository ventAnnualMonthRepository,
                          VentAnnualService ventAnnualService,
                          VentShortTermMonthRepository ventShortTermMonthRepository,
                          VentShortTermService ventShortTermService,
                          ApplicationVersionService applicationVersionService,
                          ConsentLengthService consentLengthService) {
    this.ventAnnualMonthRepository = ventAnnualMonthRepository;
    this.ventAnnualService = ventAnnualService;
    this.ventShortTermMonthRepository = ventShortTermMonthRepository;
    this.ventShortTermService = ventShortTermService;
    this.applicationVersionService = applicationVersionService;
    this.consentLengthService = consentLengthService;
  }

  @Override
  public void onApplicationEvent(ConsentLengthChangeEvent event) {
    ApplicationVersion applicationVersion = applicationVersionService
        .getApplicationVersionById(event.getApplicationVersionId());
    ApplicationType applicationType = applicationVersion.getApplication().getType();

    if (applicationType.equals(ApplicationType.VENT)) {
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
   * the given application version, and also deletes any old vent annual data if the annual consent year
   * has changed.
   *
   * @param applicationVersion the application version.
   * @param consentLengthDetails the new consent length details for the application version.
   */
  private void removeObsoleteDataWhenAnnual(ApplicationVersion applicationVersion,
                                            ConsentLengthDetails consentLengthDetails) {
    ventShortTermMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old vent data removed when consent length changed to annual for application version with id {}.",
        applicationVersion.getId());

    List<VentAnnualMonth> existingVentAnnualMonths = ventAnnualService.getVentAnnualMonths(applicationVersion);

    if (!existingVentAnnualMonths.isEmpty()) {
      int newAnnualConsentYear = consentLengthDetails.getAnnualConsentYear();

      for (VentAnnualMonth ventAnnualMonth: existingVentAnnualMonths) {
        if (ventAnnualMonth.getYear() != newAnnualConsentYear) {
          ventAnnualMonthRepository.delete(ventAnnualMonth);

          LOGGER.debug("Old vent annual data removed for month / year {} / {}.",
              ventAnnualMonth.getMonth(), ventAnnualMonth.getYear());
        }
      }
    }
  }

  /**
   * Executed when the consent length details have changed to be short term. This deletes any existing annual
   * data for the given application version, and also deletes stale vent short term data where the month term
   * is now not expected (based on the new consent length start and end dates).
   *
   * @param applicationVersion the application version.
   * @param consentLengthDetails the new consent length details for the application version.
   */
  private void removeObsoleteDataWhenShortTerm(ApplicationVersion applicationVersion,
                                               ConsentLengthDetails consentLengthDetails) {
    ventAnnualMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old vent data removed when consent length changed to short term for application version with id {}.",
        applicationVersion.getId());

    // get the existing saved vent short term months data
    List<VentShortTermMonth> existingVentShortTermMonths =
        ventShortTermService.getVentShortTermMonths(applicationVersion);

    // create a set of expected start and end date pairs, i.e. for the new short term consent length details
    // we expect to have the following month data for each of these pairs
    Set<Pair<LocalDate, LocalDate>> expectedShortTermMonthTermsSet =
        new HashSet<>(ShortTermUtil.getExpectedMonthTerms(consentLengthDetails.getShortTermStartDate(),
            consentLengthDetails.getShortTermEndDate()));

    // loop over the existing short term vent data and delete it if it now isn't an expected month row
    for (VentShortTermMonth ventShortTermMonth : existingVentShortTermMonths) {
      if (!expectedShortTermMonthTermsSet.contains(Pair.of(ventShortTermMonth.getStartDate(),
          ventShortTermMonth.getEndDate()))) {
        ventShortTermMonthRepository.delete(ventShortTermMonth);

        LOGGER.debug("Old vent short term data removed for start date / end date {} / {}.",
            ventShortTermMonth.getStartDate(), ventShortTermMonth.getEndDate());
      }
    }
  }
}

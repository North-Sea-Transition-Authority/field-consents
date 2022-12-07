package uk.co.nstauthority.fieldconsents.production;

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
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonthRepository;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYearRepository;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonthRepository;

@Service
public class ProductionRowCleanupService implements ApplicationListener<ConsentLengthChangeEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductionRowCleanupService.class);

  private final ShortTermProductionMonthRepository shortTermProductionMonthRepository;

  private final AnnualProductionMonthRepository annualProductionMonthRepository;

  private final LongTermProductionYearRepository longTermProductionYearRepository;

  private final ApplicationVersionService applicationVersionService;

  private final ConsentLengthService consentLengthService;

  @Autowired
  public ProductionRowCleanupService(ShortTermProductionMonthRepository shortTermProductionMonthRepository,
                                     AnnualProductionMonthRepository annualProductionMonthRepository,
                                     LongTermProductionYearRepository longTermProductionYearRepository,
                                     ApplicationVersionService applicationVersionService,
                                     ConsentLengthService consentLengthService) {
    this.shortTermProductionMonthRepository = shortTermProductionMonthRepository;
    this.annualProductionMonthRepository = annualProductionMonthRepository;
    this.longTermProductionYearRepository = longTermProductionYearRepository;
    this.applicationVersionService = applicationVersionService;
    this.consentLengthService = consentLengthService;
  }

  @Override
  public void onApplicationEvent(ConsentLengthChangeEvent event) {
    ApplicationVersion applicationVersion = applicationVersionService
        .getApplicationVersionById(event.getApplicationVersionId());
    ApplicationType type = applicationVersion.getApplication().getType();

    if (type.equals(ApplicationType.PRODUCTION)) {
      ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

      ConsentLengthType consentLengthType = consentLengthDetails.getConsentLength();
      switch (consentLengthType) {
        case SHORT_TERM -> removeObsoleteDataWhenShortTerm(applicationVersion, consentLengthDetails);
        case ANNUAL -> removeObsoleteDataWhenAnnual(applicationVersion, consentLengthDetails);
        case LONG_TERM -> removeObsoleteDataWhenLongTerm(applicationVersion, consentLengthDetails);
        default -> throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
      }
    }
  }

  /**
   * Executed when the consent length details have changed to be long term. This deletes any existing short term or
   * annual existing data saved with this application version. Also, it deletes old production year rows if any of the
   * following is true:
   *   1. The start year has changed, and it's now after the first production year
   *   2. The end year has changed, and it's now before the last production year
   * @param applicationVersion the version of the application with the production data being deleted.
   * @param consentLengthDetails the new consent length details for this application version.
   */
  private void removeObsoleteDataWhenLongTerm(ApplicationVersion applicationVersion,
                                              ConsentLengthDetails consentLengthDetails) {
    annualProductionMonthRepository.deleteAllByApplicationVersion(applicationVersion);
    shortTermProductionMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old production data removed when consent length changed to long term for application version with id {}.",
            applicationVersion.getId());

    Integer newStartYear = consentLengthDetails.getLongTermStartYear();
    Integer newEndYear = consentLengthDetails.getLongTermEndYear();

    List<LongTermProductionYear> existingLongTermProductionYears = longTermProductionYearRepository
        .findAllByApplicationVersionOrderByYearAsc(applicationVersion);

    if (!existingLongTermProductionYears.isEmpty()) {
      for (LongTermProductionYear eltpy : existingLongTermProductionYears) {
        if (newStartYear > (eltpy.getYear()) || newEndYear < (eltpy.getYear())) {
          longTermProductionYearRepository.delete(eltpy);

          LOGGER.debug("Old long term production data removed for year {}.", eltpy.getYear());
        }
      }
    }
  }

  /**
   * Executed when the consent length details have changed to be annual. This deletes any existing short / long term
   * existing data saved with this application version. Also, it deletes old production year rows if the annual consent
   * year has changed.
   *
   * @param applicationVersion the version of the application with the production data being deleted.
   * @param consentLengthDetails the new consent length details for this application version.
   */
  private void removeObsoleteDataWhenAnnual(ApplicationVersion applicationVersion,
                                            ConsentLengthDetails consentLengthDetails) {
    shortTermProductionMonthRepository.deleteAllByApplicationVersion(applicationVersion);
    longTermProductionYearRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old production data removed when consent length changed to annual for application version with id {}.",
        applicationVersion.getId());

    List<AnnualProductionMonth> existingAnnualProductionMonths =
        annualProductionMonthRepository.findAllByApplicationVersion(applicationVersion);

    if (!existingAnnualProductionMonths.isEmpty()) {
      int newYear = consentLengthDetails.getAnnualConsentYear();

      for (AnnualProductionMonth month : existingAnnualProductionMonths) {
        if (month.getYear() != newYear) {
          annualProductionMonthRepository.delete(month);

          LOGGER.debug("Old annual production data removed for month / year {} / {}.", month.getMonth(), month.getYear());
        }
      }
    }
  }

  /**
   * Executed when the consent length details have changed to be short term. This deletes any existing long term or
   * annual data saved with this application version. Also, it deletes old production month rows if the row isn't
   * now expected, based on the updated consent length start and end dates.
   *
   * @param applicationVersion the version of the application with the production data being deleted.
   * @param consentLengthDetails the new consent length details for this application version.
   */
  private void removeObsoleteDataWhenShortTerm(ApplicationVersion applicationVersion,
                                               ConsentLengthDetails consentLengthDetails) {
    annualProductionMonthRepository.deleteAllByApplicationVersion(applicationVersion);
    longTermProductionYearRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old production data removed when consent length changed to short term for application version with id {}.",
            applicationVersion.getId());

    // get the existing saved short term production months data
    List<ShortTermProductionMonth> existingShortTermProductionMonths = shortTermProductionMonthRepository
        .findAllByApplicationVersionOrderByStartDate(applicationVersion);

    // create a set of expected start and end date pairs, i.e. for the new short term consent length details
    // we expect to have the following month data for each of these pairs
    Set<Pair<LocalDate, LocalDate>> expectedShortTermMonthTermsSet =
        new HashSet<>(ShortTermUtil.getExpectedMonthTerms(consentLengthDetails.getShortTermStartDate(),
            consentLengthDetails.getShortTermEndDate()));

    // loop over the existing short term production data and delete it if it now isn't an expected month row
    for (ShortTermProductionMonth estpm : existingShortTermProductionMonths) {
      if (!expectedShortTermMonthTermsSet.contains(Pair.of(estpm.getStartDate(), estpm.getEndDate()))) {
        shortTermProductionMonthRepository.delete(estpm);

        LOGGER.debug("Old short term production data removed for start date / end date {} / {}.",
            estpm.getStartDate(), estpm.getEndDate());
      }
    }
  }
}

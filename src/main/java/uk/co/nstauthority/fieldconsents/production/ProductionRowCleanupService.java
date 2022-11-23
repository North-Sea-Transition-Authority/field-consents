package uk.co.nstauthority.fieldconsents.production;

import java.time.LocalDate;
import java.util.List;
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
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonthRepository;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYearRepository;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonthRepository;

@Service
public class ProductionRowCleanupService implements ApplicationListener<ConsentLengthChangeEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductionRowCleanupService.class);

  private static final String LONG_TERM_PRODUCTION_DATA_REMOVED =
      "Old long term production data removed for year / month {} / {}.";

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
        default -> throw new RuntimeException("Incorrect consent length type: " + type);
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
   * annual data saved with this application version. Also, it deletes old production month rows if any of the following
   * is true:
   *   1. The start date has changed, but it's still within the first production year / month
   *   2. The end date has changed, but it's still within the last production year / month
   *   3. The start date has changed, and it's now after the first production month start date
   *   4. The end date has changed, and it's now before the last production month end date
   * @param applicationVersion the version of the application with the production data being deleted.
   * @param consentLengthDetails the new consent length details for this application version.
   */
  private void removeObsoleteDataWhenShortTerm(ApplicationVersion applicationVersion,
                                               ConsentLengthDetails consentLengthDetails) {
    annualProductionMonthRepository.deleteAllByApplicationVersion(applicationVersion);
    longTermProductionYearRepository.deleteAllByApplicationVersion(applicationVersion);

    LOGGER.debug("Old production data removed when consent length changed to short term for application version with id {}.",
            applicationVersion.getId());

    LocalDate newStartDate = consentLengthDetails.getShortTermStartDate();
    LocalDate newEndDate = consentLengthDetails.getShortTermEndDate();

    List<ShortTermProductionMonth> existingShortTermProductionMonths = shortTermProductionMonthRepository
        .findAllByApplicationVersionOrderByStartDate(applicationVersion);

    if (!existingShortTermProductionMonths.isEmpty()) {
      ShortTermProductionMonth firstMonth = existingShortTermProductionMonths.get(0);
      if (!newStartDate.equals(firstMonth.getStartDate())) {
        shortTermProductionMonthRepository.delete(firstMonth);

        LOGGER.debug(LONG_TERM_PRODUCTION_DATA_REMOVED, firstMonth.getYear(), firstMonth.getMonth());
      }

      ShortTermProductionMonth lastMonth = existingShortTermProductionMonths.get(existingShortTermProductionMonths.size() - 1);
      if (!newEndDate.equals(lastMonth.getEndDate())) {
        shortTermProductionMonthRepository.delete(lastMonth);

        LOGGER.debug(LONG_TERM_PRODUCTION_DATA_REMOVED, lastMonth.getYear(), lastMonth.getMonth());
      }

      for (ShortTermProductionMonth estpm : existingShortTermProductionMonths) {
        if (newStartDate.isAfter(estpm.getStartDate()) || newEndDate.isBefore(estpm.getEndDate())) {
          shortTermProductionMonthRepository.delete(estpm);

          LOGGER.debug(LONG_TERM_PRODUCTION_DATA_REMOVED, estpm.getYear(), estpm.getMonth());
        }
      }
    }
  }
}

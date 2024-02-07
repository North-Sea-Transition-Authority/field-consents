package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.math.BigDecimal;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionShortTermUtil;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@Service
public class ConsentEmissionFigureService {

  private final FlareShortTermService flareShortTermService;
  private final VentShortTermService ventShortTermService;
  private final FlareAnnualService flareAnnualService;
  private final VentAnnualService ventAnnualService;

  @Autowired
  ConsentEmissionFigureService(
      FlareShortTermService flareShortTermService,
      VentShortTermService ventShortTermService,
      FlareAnnualService flareAnnualService,
      VentAnnualService ventAnnualService
  ) {
    this.flareShortTermService = flareShortTermService;
    this.ventShortTermService = ventShortTermService;
    this.flareAnnualService = flareAnnualService;
    this.ventAnnualService = ventAnnualService;
  }

  public BigDecimal getShortTermEmissionMaxRate(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();

    var shortTermConsentMonths = switch (applicationType) {
      case FLARE -> flareShortTermService.getFlareShortTermMonths(applicationVersion);
      case VENT -> ventShortTermService.getVentShortTermMonths(applicationVersion);
      default -> throw new IllegalStateException("Unexpected ApplicationType: %s".formatted(applicationType));
    };

    var totalDays = shortTermConsentMonths
        .stream()
        .mapToInt(EmissionShortTermUtil::getShortTermMonthConsentDays)
        .sum();

    var categoryATotal = BigDecimalUtil.sum(shortTermConsentMonths, FlareVentRow::getCategoryA);
    var categoryBTotal = BigDecimalUtil.sum(shortTermConsentMonths, FlareVentRow::getCategoryB);
    var categoryCTotal = BigDecimalUtil.sum(shortTermConsentMonths, FlareVentRow::getCategoryC);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    return BigDecimalUtil.divideRound(categoryTotal, totalDays);
  }

  public BigDecimal getAnnualEmissionMaxRate(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();

    var annualConsentMonths = switch (applicationType) {
      case FLARE -> flareAnnualService.getFlareAnnualMonths(applicationVersion);
      case VENT -> ventAnnualService.getVentAnnualMonths(applicationVersion);
      default -> throw new IllegalStateException("Unexpected ApplicationType: %s".formatted(applicationType));
    };

    var totalDays = annualConsentMonths
        .stream()
        .mapToInt(annualConsentMonth -> YearMonth.of(annualConsentMonth.getYear(), annualConsentMonth.getMonth()).lengthOfMonth())
        .sum();

    var categoryATotal = BigDecimalUtil.sum(annualConsentMonths, FlareVentRow::getCategoryA);
    var categoryBTotal = BigDecimalUtil.sum(annualConsentMonths, FlareVentRow::getCategoryB);
    var categoryCTotal = BigDecimalUtil.sum(annualConsentMonths, FlareVentRow::getCategoryC);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    return BigDecimalUtil.divideRound(categoryTotal, totalDays);
  }
}

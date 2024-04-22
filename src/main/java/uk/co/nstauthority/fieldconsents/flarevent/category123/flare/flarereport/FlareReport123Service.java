package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport;

import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.util.ApplicationFigureComparators;

@Service
public class FlareReport123Service {

  private final FlareReport123MonthRepository flareReport123MonthRepository;

  public FlareReport123Service(FlareReport123MonthRepository flareReport123MonthRepository) {
    this.flareReport123MonthRepository = flareReport123MonthRepository;
  }

  public List<FlareReport123Month> getFlareReport123Months(ApplicationVersion applicationVersion) {
    return flareReport123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(ApplicationFigureComparators.flare123Row())
        .toList();
  }

  public YearMonth getStartYearMonth(ApplicationVersion applicationVersion) {
    return flareReport123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .min(ApplicationFigureComparators.flare123Row())
        .map(flareReport123Month -> YearMonth.of(flareReport123Month.getYear(), flareReport123Month.getMonth()))
        .orElseThrow();
  }

  public YearMonth getEndYearMonth(ApplicationVersion applicationVersion) {
    return flareReport123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .max(ApplicationFigureComparators.flare123Row())
        .map(flareReport123Month -> YearMonth.of(flareReport123Month.getYear(), flareReport123Month.getMonth()))
        .orElseThrow();
  }
}

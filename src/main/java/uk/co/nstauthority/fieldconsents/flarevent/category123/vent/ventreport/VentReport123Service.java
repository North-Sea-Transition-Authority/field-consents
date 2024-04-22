package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport;

import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.util.ApplicationFigureComparators;

@Service
public class VentReport123Service {

  private final VentReport123MonthRepository ventReport123MonthRepository;

  public VentReport123Service(VentReport123MonthRepository ventReport123MonthRepository) {
    this.ventReport123MonthRepository = ventReport123MonthRepository;
  }

  public List<VentReport123Month> getVentReport123Months(ApplicationVersion applicationVersion) {
    return ventReport123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(ApplicationFigureComparators.vent123Row())
        .toList();
  }

  public YearMonth getStartYearMonth(ApplicationVersion applicationVersion) {
    return ventReport123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .min(ApplicationFigureComparators.vent123Row())
        .map(ventReport123Month -> YearMonth.of(ventReport123Month.getYear(), ventReport123Month.getMonth()))
        .orElseThrow();
  }

  public YearMonth getEndYearMonth(ApplicationVersion applicationVersion) {
    return ventReport123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .max(ApplicationFigureComparators.vent123Row())
        .map(ventReport123Month -> YearMonth.of(ventReport123Month.getYear(), ventReport123Month.getMonth()))
        .orElseThrow();
  }
}

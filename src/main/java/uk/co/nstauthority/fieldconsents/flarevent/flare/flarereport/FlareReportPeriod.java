package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.Month;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Table(name = "flare_report_periods")
public class FlareReportPeriod {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Boolean hasDataForPeriod;

  @Enumerated(EnumType.STRING)
  private Month reportEndMonth;

  private Integer reportEndYear;

  public FlareReportPeriod() {
  }

  public FlareReportPeriod(ApplicationVersion applicationVersion,
                           Boolean hasDataForPeriod,
                           Month reportEndMonth,
                           Integer reportEndYear) {
    this.applicationVersion = applicationVersion;
    this.hasDataForPeriod = hasDataForPeriod;
    this.reportEndMonth = reportEndMonth;
    this.reportEndYear = reportEndYear;
  }

  static FlareReportPeriod from(ApplicationVersion applicationVersion,
                                FlareReportPeriodForm flareReportPeriodForm,
                                YearMonth proposedReportEndYearMonth) {

    FlareReportPeriod flareReportPeriod = new FlareReportPeriod();
    flareReportPeriod.setApplicationVersion(applicationVersion);
    Boolean hasDataForPeriod = flareReportPeriodForm.getHasDataForPeriod();
    flareReportPeriod.setHasDataForPeriod(hasDataForPeriod);
    if (Boolean.TRUE.equals(hasDataForPeriod)) {
      flareReportPeriod.setReportEndMonth(proposedReportEndYearMonth.getMonth());
      flareReportPeriod.setReportEndYear(proposedReportEndYearMonth.getYear());
    } else {
      flareReportPeriod.setReportEndMonth(
          Month.valueOf(flareReportPeriodForm.getReportEndMonth().getInputValue().toUpperCase()));
      flareReportPeriod.setReportEndYear(flareReportPeriodForm.getReportEndYear().getAsInteger()
          .orElseThrow(NoSuchElementException::new));
    }
    return flareReportPeriod;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Boolean getHasDataForPeriod() {
    return hasDataForPeriod;
  }

  public void setHasDataForPeriod(Boolean hasDataForPeriod) {
    this.hasDataForPeriod = hasDataForPeriod;
  }

  public Month getReportEndMonth() {
    return reportEndMonth;
  }

  public void setReportEndMonth(Month reportEndMonth) {
    this.reportEndMonth = reportEndMonth;
  }

  public Integer getReportEndYear() {
    return reportEndYear;
  }

  public void setReportEndYear(Integer reportEndYear) {
    this.reportEndYear = reportEndYear;
  }

  public YearMonth getReportEndYearMonth() {
    return YearMonth.of(reportEndYear, reportEndMonth);
  }

  public YearMonth getReportStartYearMonth() {
    return getReportEndYearMonth().minusMonths(11);
  }

}

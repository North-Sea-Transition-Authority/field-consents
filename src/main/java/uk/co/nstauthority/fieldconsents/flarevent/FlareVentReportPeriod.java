package uk.co.nstauthority.fieldconsents.flarevent;

import java.time.Month;
import java.time.YearMonth;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@MappedSuperclass
public class FlareVentReportPeriod {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Enumerated(EnumType.STRING)
  private Month reportEndMonth;

  private Integer reportEndYear;

  public FlareVentReportPeriod() {
  }

  public FlareVentReportPeriod(ApplicationVersion applicationVersion,
                               Month reportEndMonth,
                               Integer reportEndYear) {
    this.applicationVersion = applicationVersion;
    this.reportEndMonth = reportEndMonth;
    this.reportEndYear = reportEndYear;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
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

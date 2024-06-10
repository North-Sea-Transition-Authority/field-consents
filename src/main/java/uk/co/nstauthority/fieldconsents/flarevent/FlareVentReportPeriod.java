package uk.co.nstauthority.fieldconsents.flarevent;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.Month;
import java.time.YearMonth;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Audited
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

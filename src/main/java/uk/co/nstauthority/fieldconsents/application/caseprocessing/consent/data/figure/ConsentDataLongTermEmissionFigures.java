package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import uk.co.nstauthority.fieldconsents.application.Application;

@Audited
@Entity
@Table(name = "application_consent_data_long_term_emission_figures")
public class ConsentDataLongTermEmissionFigures {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_id")
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  private Application application;

  private Integer year;

  private BigDecimal dailyAverage;

  public ConsentDataLongTermEmissionFigures() {
  }

  public ConsentDataLongTermEmissionFigures(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public BigDecimal getDailyAverage() {
    return dailyAverage;
  }

  public void setDailyAverage(BigDecimal dailyAverage) {
    this.dailyAverage = dailyAverage;
  }
}

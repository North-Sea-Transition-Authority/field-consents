package uk.co.nstauthority.fieldconsents.flarevent.category123.vent;


import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.math.BigDecimal;
import java.time.Month;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

/**
 * This class represents all entities with an id and other category details related to vent 123 data.
 */
@MappedSuperclass
public class Vent123Row {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Integer year;

  @Enumerated(EnumType.STRING)
  private Month month;

  @Column(name = "category_1")
  private BigDecimal category1;

  private String comments;

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Month getMonth() {
    return month;
  }

  public void setMonth(Month month) {
    this.month = month;
  }

  public BigDecimal getCategory1() {
    return category1;
  }

  public void setCategory1(BigDecimal category1) {
    this.category1 = category1;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }
}

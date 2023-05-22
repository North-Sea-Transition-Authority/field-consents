package uk.co.nstauthority.fieldconsents.flarevent;


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
import java.util.NoSuchElementException;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

/**
 * This class represents all entities with an id and other category details related to flare and vent forms.
 */
@MappedSuperclass
public class FlareVentRow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Integer year;

  @Enumerated(EnumType.STRING)
  private Month month;

  @Column(name = "category_a")
  private BigDecimal categoryA;

  @Column(name = "category_b")
  private BigDecimal categoryB;

  @Column(name = "category_c")
  private BigDecimal categoryC;

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

  public BigDecimal getCategoryA() {
    return categoryA;
  }

  public void setCategoryA(BigDecimal categoryA) {
    this.categoryA = categoryA;
  }

  public BigDecimal getCategoryB() {
    return categoryB;
  }

  public void setCategoryB(BigDecimal categoryB) {
    this.categoryB = categoryB;
  }

  public BigDecimal getCategoryC() {
    return categoryC;
  }

  public void setCategoryC(BigDecimal categoryC) {
    this.categoryC = categoryC;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public void updateFlareVentRowFromForm(ApplicationVersion applicationVersion, FlareVentRowForm form) {
    this.applicationVersion = applicationVersion;
    this.year = Integer.parseInt(form.getYear());
    this.month = Month.valueOf(form.getMonth().toUpperCase());
    this.categoryA = form.getCategoryA().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    this.categoryB = form.getCategoryB().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    this.categoryC = form.getCategoryC().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    this.comments = form.getComments().getInputValue();
  }
}

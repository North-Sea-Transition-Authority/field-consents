package uk.co.nstauthority.fieldconsents.flarevent;


import java.math.BigDecimal;
import java.util.NoSuchElementException;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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

  @Column(name = "category_a")
  private BigDecimal categoryA;

  @Column(name = "category_b")
  private BigDecimal categoryB;

  @Column(name = "category_c")
  private BigDecimal categoryC;

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
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

  public void updateFlareVentRowFromForm(FlareVentRowForm form) {
    this.categoryA = form.getCategoryA().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    this.categoryB = form.getCategoryB().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    this.categoryC = form.getCategoryC().getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
  }
}

package uk.co.nstauthority.fieldconsents.flarevent;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@MappedSuperclass
public class FlareVentReportGasData {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  @Column(name = "category_a_density")
  private BigDecimal categoryADensity;

  @Column(name = "category_a_inert_percentage")
  private BigDecimal categoryAInertGasPercentage;

  @Column(name = "category_a_hydro_percentage")
  private BigDecimal categoryAHydrocarbonPercentage;

  @Column(name = "category_b_density")
  private BigDecimal categoryBDensity;

  @Column(name = "category_b_inert_percentage")
  private BigDecimal categoryBInertGasPercentage;

  @Column(name = "category_b_hydro_percentage")
  private BigDecimal categoryBHydrocarbonPercentage;

  @Column(name = "category_c_density")
  private BigDecimal categoryCDensity;

  @Column(name = "category_c_inert_percentage")
  private BigDecimal categoryCInertGasPercentage;

  @Column(name = "category_c_hydro_percentage")
  private BigDecimal categoryCHydrocarbonPercentage;

  private Boolean evaluatedPerCategory;

  private String evaluatedPerCategoryExplanation;

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public BigDecimal getCategoryADensity() {
    return categoryADensity;
  }

  public void setCategoryADensity(BigDecimal categoryADensity) {
    this.categoryADensity = categoryADensity;
  }

  public BigDecimal getCategoryAInertGasPercentage() {
    return categoryAInertGasPercentage;
  }

  public void setCategoryAInertGasPercentage(BigDecimal categoryAInertGasPercentage) {
    this.categoryAInertGasPercentage = categoryAInertGasPercentage;
  }

  public BigDecimal getCategoryAHydrocarbonPercentage() {
    return categoryAHydrocarbonPercentage;
  }

  public void setCategoryAHydrocarbonPercentage(BigDecimal categoryAHydrocarbonPercentage) {
    this.categoryAHydrocarbonPercentage = categoryAHydrocarbonPercentage;
  }

  public BigDecimal getCategoryBDensity() {
    return categoryBDensity;
  }

  public void setCategoryBDensity(BigDecimal categoryBDensity) {
    this.categoryBDensity = categoryBDensity;
  }

  public BigDecimal getCategoryBInertGasPercentage() {
    return categoryBInertGasPercentage;
  }

  public void setCategoryBInertGasPercentage(BigDecimal categoryBInertGasPercentage) {
    this.categoryBInertGasPercentage = categoryBInertGasPercentage;
  }

  public BigDecimal getCategoryBHydrocarbonPercentage() {
    return categoryBHydrocarbonPercentage;
  }

  public void setCategoryBHydrocarbonPercentage(BigDecimal categoryBHydrocarbonPercentage) {
    this.categoryBHydrocarbonPercentage = categoryBHydrocarbonPercentage;
  }

  public BigDecimal getCategoryCDensity() {
    return categoryCDensity;
  }

  public void setCategoryCDensity(BigDecimal categoryCDensity) {
    this.categoryCDensity = categoryCDensity;
  }

  public BigDecimal getCategoryCInertGasPercentage() {
    return categoryCInertGasPercentage;
  }

  public void setCategoryCInertGasPercentage(BigDecimal categoryCInertGasPercentage) {
    this.categoryCInertGasPercentage = categoryCInertGasPercentage;
  }

  public BigDecimal getCategoryCHydrocarbonPercentage() {
    return categoryCHydrocarbonPercentage;
  }

  public void setCategoryCHydrocarbonPercentage(BigDecimal categoryCHydrocarbonPercentage) {
    this.categoryCHydrocarbonPercentage = categoryCHydrocarbonPercentage;
  }

  public Boolean getEvaluatedPerCategory() {
    return evaluatedPerCategory;
  }

  public void setEvaluatedPerCategory(Boolean areInformationCorrect) {
    this.evaluatedPerCategory = areInformationCorrect;
  }

  public String getEvaluatedPerCategoryExplanation() {
    return evaluatedPerCategoryExplanation;
  }

  public void setEvaluatedPerCategoryExplanation(String reason) {
    this.evaluatedPerCategoryExplanation = reason;
  }

  public void updateFlareVentReportGasDataFromForm(ApplicationVersion applicationVersion,
                                                   FlareVentReportGasDataForm reportGasDataForm) {
    setApplicationVersion(applicationVersion);
    setCategoryADensity(reportGasDataForm.getCategoryADensity().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));
    setCategoryAInertGasPercentage(reportGasDataForm.getCategoryAInertGasPercentage().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));
    setCategoryAHydrocarbonPercentage(reportGasDataForm.getCategoryAHydrocarbonPercentage().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));

    setCategoryBDensity(reportGasDataForm.getCategoryBDensity().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));
    setCategoryBInertGasPercentage(reportGasDataForm.getCategoryBInertGasPercentage().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));
    setCategoryBHydrocarbonPercentage(reportGasDataForm.getCategoryBHydrocarbonPercentage().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));

    setCategoryCDensity(reportGasDataForm.getCategoryCDensity().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));
    setCategoryCInertGasPercentage(reportGasDataForm.getCategoryCInertGasPercentage().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));
    setCategoryCHydrocarbonPercentage(reportGasDataForm.getCategoryCHydrocarbonPercentage().getAsBigDecimal().orElseThrow(
        NoSuchElementException::new));

    setEvaluatedPerCategory(reportGasDataForm.getEvaluatedPerCategory());
    if (Boolean.FALSE.equals(reportGasDataForm.getEvaluatedPerCategory())) {
      this.setEvaluatedPerCategoryExplanation(reportGasDataForm.getEvaluatedPerCategoryExplanation().getInputValue());
    }
  }
}

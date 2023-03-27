package uk.co.nstauthority.fieldconsents.flarevent;

import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

public class FlareVentReportGasDataForm {

  private final DecimalInput categoryADensity;

  private final DecimalInput categoryAInertGasPercentage;

  private final DecimalInput categoryAHydrocarbonPercentage;

  private final DecimalInput categoryBDensity;

  private final DecimalInput categoryBInertGasPercentage;

  private final DecimalInput categoryBHydrocarbonPercentage;

  private final DecimalInput categoryCDensity;

  private final DecimalInput categoryCInertGasPercentage;

  private final DecimalInput categoryCHydrocarbonPercentage;

  private Boolean evaluatedPerCategory;

  private final StringInput evaluatedPerCategoryExplanation;


  public FlareVentReportGasDataForm() {
    this.categoryADensity = new DecimalInput("categoryADensity", "Category A standard density");
    this.categoryAInertGasPercentage = new DecimalInput("categoryAInertGasPercentage", "Category A inert gas content");
    this.categoryAHydrocarbonPercentage = new DecimalInput("categoryAHydrocarbonPercentage", "Category A hydrocarbon content");
    this.categoryBDensity = new DecimalInput("categoryBDensity", "Category B standard density");
    this.categoryBInertGasPercentage = new DecimalInput("categoryBInertGasPercentage", "Category B inert gas content");
    this.categoryBHydrocarbonPercentage = new DecimalInput("categoryBHydrocarbonPercentage", "Category B hydrocarbon content");
    this.categoryCDensity = new DecimalInput("categoryCDensity", "Category C standard density");
    this.categoryCInertGasPercentage = new DecimalInput("categoryCInertGasPercentage", "Category C inert gas content");
    this.categoryCHydrocarbonPercentage = new DecimalInput("categoryCHydrocarbonPercentage", "Category C hydrocarbon content");
    this.evaluatedPerCategoryExplanation = new StringInput("evaluatedPerCategoryExplanation", "an explanation");
  }

  public DecimalInput getCategoryADensity() {
    return categoryADensity;
  }

  public void setCategoryADensity(String categoryADensity) {
    this.categoryADensity.setInputValue(categoryADensity);
  }

  public DecimalInput getCategoryAInertGasPercentage() {
    return categoryAInertGasPercentage;
  }

  public void setCategoryAInertGasPercentage(String categoryAInertGasPercentage) {
    this.categoryAInertGasPercentage.setInputValue(categoryAInertGasPercentage);
  }

  public DecimalInput getCategoryAHydrocarbonPercentage() {
    return categoryAHydrocarbonPercentage;
  }

  public void setCategoryAHydrocarbonPercentage(String categoryAHydrocarbonPercentage) {
    this.categoryAHydrocarbonPercentage.setInputValue(categoryAHydrocarbonPercentage);
  }

  public DecimalInput getCategoryBDensity() {
    return categoryBDensity;
  }

  public void setCategoryBDensity(String categoryBDensity) {
    this.categoryBDensity.setInputValue(categoryBDensity);
  }

  public DecimalInput getCategoryBInertGasPercentage() {
    return categoryBInertGasPercentage;
  }

  public void setCategoryBInertGasPercentage(String categoryBInertGasPercentage) {
    this.categoryBInertGasPercentage.setInputValue(categoryBInertGasPercentage);
  }

  public DecimalInput getCategoryBHydrocarbonPercentage() {
    return categoryBHydrocarbonPercentage;
  }

  public void setCategoryBHydrocarbonPercentage(String categoryBHydrocarbonPercentage) {
    this.categoryBHydrocarbonPercentage.setInputValue(categoryBHydrocarbonPercentage);
  }

  public DecimalInput getCategoryCDensity() {
    return categoryCDensity;
  }

  public void setCategoryCDensity(String categoryCDensity) {
    this.categoryCDensity.setInputValue(categoryCDensity);
  }

  public DecimalInput getCategoryCInertGasPercentage() {
    return categoryCInertGasPercentage;
  }

  public void setCategoryCInertGasPercentage(String categoryCInertGasPercentage) {
    this.categoryCInertGasPercentage.setInputValue(categoryCInertGasPercentage);
  }

  public DecimalInput getCategoryCHydrocarbonPercentage() {
    return categoryCHydrocarbonPercentage;
  }

  public void setCategoryCHydrocarbonPercentage(String categoryCHydrocarbonPercentage) {
    this.categoryCHydrocarbonPercentage.setInputValue(categoryCHydrocarbonPercentage);
  }

  public Boolean getEvaluatedPerCategory() {
    return evaluatedPerCategory;
  }

  public void setEvaluatedPerCategory(Boolean evaluatedPerCategory) {
    this.evaluatedPerCategory = evaluatedPerCategory;
  }

  public StringInput getEvaluatedPerCategoryExplanation() {
    return evaluatedPerCategoryExplanation;
  }

  public void setEvaluatedPerCategoryExplanation(String evaluatedPerCategoryExplanation) {
    this.evaluatedPerCategoryExplanation.setInputValue(evaluatedPerCategoryExplanation);
  }

  public static FlareVentReportGasDataForm from(FlareVentReportGasData flareVentReportGasData) {
    FlareVentReportGasDataForm reportGasDataForm = new FlareVentReportGasDataForm();
    reportGasDataForm.setCategoryADensity(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryADensity()));
    reportGasDataForm.setCategoryAInertGasPercentage(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryAInertGasPercentage()));
    reportGasDataForm.setCategoryAHydrocarbonPercentage(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryAHydrocarbonPercentage()));

    reportGasDataForm.setCategoryBDensity(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryBDensity()));
    reportGasDataForm.setCategoryBInertGasPercentage(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryBInertGasPercentage()));
    reportGasDataForm.setCategoryBHydrocarbonPercentage(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryBHydrocarbonPercentage()));

    reportGasDataForm.setCategoryCDensity(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryCDensity()));
    reportGasDataForm.setCategoryCInertGasPercentage(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryCInertGasPercentage()));
    reportGasDataForm.setCategoryCHydrocarbonPercentage(DecimalFormatUtils
        .bigDecimalToFormattedString(flareVentReportGasData.getCategoryCHydrocarbonPercentage()));

    reportGasDataForm.setEvaluatedPerCategory(flareVentReportGasData.getEvaluatedPerCategory());
    reportGasDataForm.setEvaluatedPerCategoryExplanation(flareVentReportGasData.getEvaluatedPerCategoryExplanation());

    return reportGasDataForm;
  }
}

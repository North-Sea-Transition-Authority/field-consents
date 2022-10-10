package uk.co.nstauthority.fieldconsents.production.annual;

import java.util.List;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

public class AnnualProductionForm {

  private String year;
  private List<AnnualProductionMonthForm> annualProductionMonthForms;

  public AnnualProductionForm() {
  }

  public AnnualProductionForm(List<AnnualProductionMonthForm> annualProductionMonthForms, String year) {
    this.annualProductionMonthForms = annualProductionMonthForms;
    this.year = year;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public List<AnnualProductionMonthForm> getAnnualProductionMonthForms() {
    return annualProductionMonthForms;
  }

  public void setAnnualProductionMonthForms(List<AnnualProductionMonthForm> annualProductionMonthForms) {
    this.annualProductionMonthForms = annualProductionMonthForms;
  }

  public ProductionUnit getOilUnit() {
    return this.annualProductionMonthForms.size() > 0 ? this.annualProductionMonthForms.get(0).getOilMinUnit() : null;
  }

  public ProductionUnit getGasUnit() {
    return this.annualProductionMonthForms.size() > 0 ? this.annualProductionMonthForms.get(0).getGasMinUnit() : null;
  }

}

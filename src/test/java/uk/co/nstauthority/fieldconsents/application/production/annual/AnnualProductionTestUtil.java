package uk.co.nstauthority.fieldconsents.application.production.annual;

import java.math.BigDecimal;
import java.time.Month;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionForm;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonthForm;

/**
 * This util class is used to provide dummy data used for the annual production controller and service tests
 */
public class AnnualProductionTestUtil {
  static public final String PRODUCTION_YEAR = "2022";

  static AnnualProductionForm getEmptyAnnualProductionForm() {
    AnnualProductionForm annualProductionForm = new AnnualProductionForm();
    List<AnnualProductionMonthForm> annualProductionMonthForms = new LinkedList<>();

    for (Month month : Month.values()) {
      AnnualProductionMonthForm monthForm = getEmptyAnnualProductionMonthForm(month);
      annualProductionMonthForms.add(monthForm);
    }

    annualProductionForm.setAnnualProductionMonthForms(annualProductionMonthForms);
    return annualProductionForm;
  }

  public static AnnualProductionForm getCompleteAnnualProductionForm() {
    AnnualProductionForm annualProductionForm = new AnnualProductionForm();
    List<AnnualProductionMonthForm> annualProductionMonthForms = new LinkedList<>();

    for (Month month : Month.values()) {
      AnnualProductionMonthForm monthForm = getCompleteAnnualProductionMonthForm(month);
      annualProductionMonthForms.add(monthForm);
    }

    annualProductionForm.setAnnualProductionMonthForms(annualProductionMonthForms);
    return annualProductionForm;
  }

  @NotNull
  public static AnnualProductionMonthForm getCompleteAnnualProductionMonthForm(Month month) {
    AnnualProductionMonthForm monthForm = getEmptyAnnualProductionMonthForm(month);

    monthForm.getOilMinValue().setInputValue(String.valueOf(0.5));
    monthForm.getOilMaxValue().setInputValue(String.valueOf(2.3));
    monthForm.getGasMinValue().setInputValue(String.valueOf(1.72));
    monthForm.getGasMaxValue().setInputValue(String.valueOf(4.25));
    return monthForm;
  }

  @NotNull
  private static AnnualProductionMonthForm getEmptyAnnualProductionMonthForm(Month month) {
    return new AnnualProductionMonthForm(month.name(),
        new DecimalInput("oilMinValue", "Minimum oil"),
        new DecimalInput("oilMaxValue", "Maximum oil"),
        new DecimalInput("gasMinValue", "Minimum gas"),
        new DecimalInput("gasMaxValue", "Maximum gas")
    );
  }

  public static List<AnnualProductionMonth> getAnnualProductionMonthsData(ApplicationVersion applicationVersion) {
    List<AnnualProductionMonth> annualProductionMonths = new LinkedList<>();

    var allMonths = Month.values();

    for(int index = 0; index < Month.values().length; index++) {
      Month monthName = allMonths[index];
      AnnualProductionMonth annualProductionMonth = getAnnualProductionMonth(applicationVersion, index, monthName);
      annualProductionMonths.add(annualProductionMonth);
    }

    return annualProductionMonths;
  }

  @NotNull
  public static AnnualProductionMonth getAnnualProductionMonth(ApplicationVersion applicationVersion,
                                                               int annualProductionMonthId,
                                                               Month month) {
    AnnualProductionMonth annualProductionMonth = new AnnualProductionMonth();
    annualProductionMonth.setId(annualProductionMonthId);
    annualProductionMonth.setApplicationVersion(applicationVersion);
    annualProductionMonth.setYear(Integer.parseInt(PRODUCTION_YEAR));
    annualProductionMonth.setMonth(month);
    annualProductionMonth.setOilMinUnit(ProductionUnit.SCM_PER_MONTH);
    annualProductionMonth.setOilMinValue(new BigDecimal("0.5"));
    annualProductionMonth.setOilMaxUnit(ProductionUnit.SCM_PER_MONTH);
    annualProductionMonth.setOilMaxValue(new BigDecimal("2.3"));
    annualProductionMonth.setGasMinUnit(ProductionUnit.KSCM_PER_MONTH);
    annualProductionMonth.setGasMinValue(new BigDecimal("1.72"));
    annualProductionMonth.setGasMaxUnit(ProductionUnit.KSCM_PER_MONTH);
    annualProductionMonth.setGasMaxValue(new BigDecimal("4.25"));

    return annualProductionMonth;
  }
}

package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas.FlareReportGasData;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas.VentReportGasData;

public class FlareVentReportGasTestUtil {

  public static final String FIRST_MONTH_REPORTING_PERIOD = "November 2021";

  public static final String LAST_MONTH_REPORTING_PERIOD = "October 2022";

  public static final Double CATEGORY_A_DENSITY = 12.4;
  public static final Double CATEGORY_A_INERT_GAS = 25.5;
  public static final Double CATEGORY_A_HYDROCARBON = 74.5;
  public static final Double CATEGORY_B_DENSITY = 0.745;
  public static final Double CATEGORY_B_INERT_GAS = 45.761;
  public static final Double CATEGORY_B_HYDROCARBON = 54.239;
  public static final Double CATEGORY_C_DENSITY = 123.0052;
  public static final Double CATEGORY_C_INERT_GAS = 52.0475;
  public static final Double CATEGORY_C_HYDROCARBON = 47.9525;

  public static ApplicationVersion flareAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(
      ApplicationType.FLARE);

  public static FlareReportGasData getCompleteAndValidFlareReportGasData() {
    FlareReportGasData reportGasData = new FlareReportGasData();
    setReportGasData(reportGasData);
    return  reportGasData;
  }

  public static VentReportGasData getCompleteAndValidVentReportGasData() {
    VentReportGasData reportGasData = new VentReportGasData();
    setReportGasData(reportGasData);
    return  reportGasData;
  }

  private static void setReportGasData(FlareVentReportGasData reportGasData) {
    reportGasData.setApplicationVersion(flareAppVersion);
    reportGasData.setCategoryADensity(BigDecimal.valueOf(CATEGORY_A_DENSITY));
    reportGasData.setCategoryAInertGasPercentage(BigDecimal.valueOf(CATEGORY_A_INERT_GAS));
    reportGasData.setCategoryAHydrocarbonPercentage(BigDecimal.valueOf(CATEGORY_A_HYDROCARBON));
    reportGasData.setCategoryBDensity(BigDecimal.valueOf(CATEGORY_B_DENSITY));
    reportGasData.setCategoryBInertGasPercentage(BigDecimal.valueOf(CATEGORY_B_INERT_GAS));
    reportGasData.setCategoryBHydrocarbonPercentage(BigDecimal.valueOf(CATEGORY_B_HYDROCARBON));
    reportGasData.setCategoryCDensity(BigDecimal.valueOf(CATEGORY_C_DENSITY));
    reportGasData.setCategoryCInertGasPercentage(BigDecimal.valueOf(CATEGORY_C_INERT_GAS));
    reportGasData.setCategoryCHydrocarbonPercentage(BigDecimal.valueOf(CATEGORY_C_HYDROCARBON));
    reportGasData.setEvaluatedPerCategory(true);
    reportGasData.setEvaluatedPerCategoryExplanation(null);
  }

  public static FlareVentReportGasDataForm getValidFlareVentReportGasDataForm() {
    FlareVentReportGasDataForm form = new FlareVentReportGasDataForm();
    form.setCategoryADensity(String.valueOf(CATEGORY_A_DENSITY));
    form.setCategoryAInertGasPercentage(String.valueOf(CATEGORY_A_INERT_GAS));
    form.setCategoryAHydrocarbonPercentage(String.valueOf(CATEGORY_A_HYDROCARBON));

    form.setCategoryBDensity(String.valueOf(CATEGORY_B_DENSITY));
    form.setCategoryBInertGasPercentage(String.valueOf(CATEGORY_B_INERT_GAS));
    form.setCategoryBHydrocarbonPercentage(String.valueOf(CATEGORY_B_HYDROCARBON));

    form.setCategoryCDensity(String.valueOf(CATEGORY_C_DENSITY));
    form.setCategoryCInertGasPercentage(String.valueOf(CATEGORY_C_INERT_GAS));
    form.setCategoryCHydrocarbonPercentage(String.valueOf(CATEGORY_C_HYDROCARBON));

    form.setEvaluatedPerCategory(Boolean.TRUE);
    form.setEvaluatedPerCategoryExplanation(null);

    return form;
  }

  public static void verifyCategoryData(FlareVentReportGasDataForm form, FlareVentReportGasData savedReportGasData) {
    assertThat(savedReportGasData.getCategoryADensity()).isEqualTo(form.getCategoryADensity().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryAInertGasPercentage()).isEqualTo(form.getCategoryAInertGasPercentage().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryAHydrocarbonPercentage()).isEqualTo(form.getCategoryAHydrocarbonPercentage().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryBDensity()).isEqualTo(form.getCategoryBDensity().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryBInertGasPercentage()).isEqualTo(form.getCategoryBInertGasPercentage().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryBHydrocarbonPercentage()).isEqualTo(form.getCategoryBHydrocarbonPercentage().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryCDensity()).isEqualTo(form.getCategoryCDensity().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryCInertGasPercentage()).isEqualTo(form.getCategoryCInertGasPercentage().getAsBigDecimal().get());
    assertThat(savedReportGasData.getCategoryCHydrocarbonPercentage()).isEqualTo(form.getCategoryCHydrocarbonPercentage().getAsBigDecimal().get());
  }
}

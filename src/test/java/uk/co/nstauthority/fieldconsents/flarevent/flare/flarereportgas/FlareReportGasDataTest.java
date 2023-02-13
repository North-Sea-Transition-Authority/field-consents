package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasTestUtil;

@ExtendWith(MockitoExtension.class)
class FlareReportGasDataTest {

  private FlareVentReportGasDataForm form;

  private ApplicationVersion flareAppVersion;

  @BeforeEach
  void setUp() {
    flareAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    form = FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm();
  }

  @Test
  void from() {
    FlareReportGasData flareReportGasData = FlareReportGasData.from(flareAppVersion, form);

    assertThat(flareReportGasData)
        .extracting(
            FlareReportGasData::getCategoryADensity,
            FlareReportGasData::getCategoryAInertGasPercentage,
            FlareReportGasData::getCategoryAHydrocarbonPercentage,
            FlareReportGasData::getCategoryBDensity,
            FlareReportGasData::getCategoryBInertGasPercentage,
            FlareReportGasData::getCategoryBHydrocarbonPercentage,
            FlareReportGasData::getCategoryCDensity,
            FlareReportGasData::getCategoryCInertGasPercentage,
            FlareReportGasData::getCategoryCHydrocarbonPercentage,
            FlareReportGasData::getEvaluatedPerCategory,
            FlareReportGasData::getEvaluatedPerCategoryExplanation
        )
        .containsExactly(
            form.getCategoryADensity().getAsBigDecimal().get(),
            form.getCategoryAInertGasPercentage().getAsBigDecimal().get(),
            form.getCategoryAHydrocarbonPercentage().getAsBigDecimal().get(),
            form.getCategoryBDensity().getAsBigDecimal().get(),
            form.getCategoryBInertGasPercentage().getAsBigDecimal().get(),
            form.getCategoryBHydrocarbonPercentage().getAsBigDecimal().get(),
            form.getCategoryCDensity().getAsBigDecimal().get(),
            form.getCategoryCInertGasPercentage().getAsBigDecimal().get(),
            form.getCategoryCHydrocarbonPercentage().getAsBigDecimal().get(),
            form.getEvaluatedPerCategory(),
            form.getEvaluatedPerCategoryExplanation().getInputValue()
        );
  }
}
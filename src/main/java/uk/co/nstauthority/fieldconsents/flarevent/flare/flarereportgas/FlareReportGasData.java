package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

import javax.persistence.Entity;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasData;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;

@Entity
@Table(name = "flare_report_gas_data")
public class FlareReportGasData extends FlareVentReportGasData {

  public static FlareReportGasData from(ApplicationVersion applicationVersion,
                                        FlareVentReportGasDataForm reportGasDataForm) {
    FlareReportGasData reportGasData = new FlareReportGasData();

    reportGasData.updateFlareVentReportGasDataFromForm(applicationVersion, reportGasDataForm);
    return reportGasData;
  }
}

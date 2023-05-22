package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasData;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;

@Entity
@Table(name = "vent_report_gas_data")
public class VentReportGasData extends FlareVentReportGasData {

  public static VentReportGasData from(ApplicationVersion applicationVersion,
                                       FlareVentReportGasDataForm reportGasDataForm) {
    VentReportGasData reportGasData = new VentReportGasData();

    reportGasData.updateFlareVentReportGasDataFromForm(applicationVersion, reportGasDataForm);
    return reportGasData;
  }
}

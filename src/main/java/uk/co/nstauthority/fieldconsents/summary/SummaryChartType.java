package uk.co.nstauthority.fieldconsents.summary;

public enum SummaryChartType {
  STACKED_BAR_CHART("fcs-stacked-bar-chart"),
  FLOATING_BAR_CHART("fcs-floating-bar-chart");

  private final String dataModule;

  SummaryChartType(String dataModule) {
    this.dataModule = dataModule;
  }

  public String getDataModule() {
    return dataModule;
  }
}

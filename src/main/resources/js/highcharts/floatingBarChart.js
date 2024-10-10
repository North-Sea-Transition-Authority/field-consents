import Highcharts from "highcharts";
import highchartsMore from "highcharts/highcharts-more";
import highchartsAccessibility from "highcharts/modules/accessibility";
import highchartsExporting from "highcharts/modules/exporting"
import highCartsOfflineExporting from "highcharts/modules/offline-exporting"

highchartsMore(Highcharts);
highchartsAccessibility(Highcharts);
highchartsExporting(Highcharts);
highCartsOfflineExporting(Highcharts);

export default class FloatingBarChart {

  constructor(element) {
    this.element = element;
    this.init().then()
  }

  async init() {
    const chartDataJson = JSON.parse(this.element.getAttribute("data-chart-json"));

    Highcharts.chart(this.element, {
      chart: {
        type: 'column',
        styledMode: true,
      },
      exporting: {
        // Older browsers may not support exporting on the client so they'll fall back to using the Highcharts servers.
        // https://www.highcharts.com/docs/export-module/client-side-export
        fallbackToExportServer: false
      },
      credits: {
        enabled: false,
      },
      title: {
        text: chartDataJson.title
      },
      xAxis: {
        categories: chartDataJson.xAxisCategories,
        title: {
          text: chartDataJson.xAxisTitleText
        }
      },
      yAxis: {
        reversedStacks: false,
        title: {
          text: chartDataJson.yAxisTitleText
        },
      },
      plotOptions: {
        column: {
          stacking: 'normal'
        },
      },
      series: chartDataJson.series
    });
  }

}

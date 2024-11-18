<#macro chart chartDataModule chartDataJson>
  <div
    data-module="${chartDataModule}"
    data-chart-json="${chartDataJson}"
    class="govuk-!-padding-bottom-2 highcharts-light"
    style="width:100%; height:600px;"
  ></div>
</#macro>

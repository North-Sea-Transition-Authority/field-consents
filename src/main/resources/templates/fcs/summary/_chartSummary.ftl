<#include '../layout/layout.ftl'>
<#import '../graphing/chart.ftl' as chart>

<#-- @ftlvariable name="summaryChart" type="uk.co.nstauthority.fieldconsents.summary.SummaryChart" -->

<#macro chartSummary summaryChart>
  <@fdsSummaryList.summaryListCard summaryListId="summary-data-card-list">
    <@chart.chart summaryChart.chartType().getDataModule() summaryChart.chartDataJson()/>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#include '../layout/layout.ftl'>
<#import '../graphing/stackedBarChart.ftl' as chart>

<#macro stackedBarChart chartDataJson>
  <@fdsSummaryList.summaryListCard summaryListId="summary-data-card-list">
    <@chart.stackedBarChart chartDataJson/>
  </@fdsSummaryList.summaryListCard>
</#macro>
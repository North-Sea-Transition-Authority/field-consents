<#include '../layout/layout.ftl'>
<#import '_simpleSummary.ftl' as simpleSummary>
<#import '_tableSummary.ftl' as tableSummary>
<#import '_emptySummary.ftl' as emptySummary>
<#import '_filesSummary.ftl' as filesSummary>
<#import '_stackedBarChartSummary.ftl' as stackedBarChartSummary>

<#-- @ftlvariable name="summaryItem" type="uk.co.nstauthority.fieldconsents.summary.SummaryItem" -->
<#-- @ftlvariable name="summaryCard" type="uk.co.nstauthority.fieldconsents.summary.SummaryCard" -->

<#macro summaryDetails summaryItem>
  <#list summaryItem.summaryCards() as summaryCard>
    <#if summaryCard.summaryCardType() == "SIMPLE_SUMMARY">
      <@simpleSummary.simpleSummary
        summaryDataView=summaryCard.summaryData()
        summaryHeading=summaryCard.displayName()!""/>
    <#elseif summaryCard.summaryCardType() == "FILES_SUMMARY">
      <@filesSummary.summary
        heading=summaryCard.displayName()!""
        fileViews=summaryCard.summaryData()/>
    <#elseif summaryCard.summaryCardType() == "TABLE_SUMMARY">
      <@tableSummary.tableSummary
        summaryTableView=summaryCard.summaryData()
        summaryHeading=summaryCard.displayName()!""/>
    <#elseif summaryCard.summaryCardType() == "EMPTY_SUMMARY">
      <@emptySummary.emptySummary/>
    <#elseif summaryCard.summaryCardType() == "STACKED_BAR_CHART_SUMMARY">
      <@stackedBarChartSummary.stackedBarChart chartDataJson=summaryCard.summaryData()/>
    </#if>
  </#list>
</#macro>

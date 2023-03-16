<#include '../layout/layout.ftl'>
<#import '_emptySummary.ftl' as emptySummary>

<#-- @ftlvariable name="summaryDataView" type="uk.co.nstauthority.fieldconsents.summary.SummaryDataView" -->

<#macro simpleSummary summaryDataView summaryHeading>
  <@fdsSummaryList.summaryListCard
    headingText=summaryHeading
    headingSize="h3"
    summaryListId="summary-data-card-list">
    <#list summaryDataView.keyValues() as keyValue>
      <@fdsSummaryList.summaryListRowNoAction keyText=keyValue.key()>
        ${(keyValue.value())!""}
      </@fdsSummaryList.summaryListRowNoAction>
    </#list>
  </@fdsSummaryList.summaryListCard>
</#macro>
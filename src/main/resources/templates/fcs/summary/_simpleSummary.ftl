<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="summaryDataView" type="uk.co.nstauthority.fieldconsents.summary.SummaryDataView" -->

<#macro simpleSummary summaryDataView summaryHeading>
  <@fdsSummaryList.summaryListCard headingText=summaryHeading summaryListId="summary-data-card-list">
      <#list summaryDataView.dataList() as data>
        <@fdsSummaryList.summaryListRowNoAction keyText=data.key()>
          ${(data.value())!""}
        </@fdsSummaryList.summaryListRowNoAction>
      </#list>
  </@fdsSummaryList.summaryListCard>
</#macro>
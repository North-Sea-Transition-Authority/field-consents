<#include '../layout/layout.ftl'>

<#macro withdrawalRequestSummary withdrawalRequest>
  <@fdsSummaryList.summaryListCard headingText="Withdrawal request for ${withdrawalRequest.applicationReference()}" summaryListId="withdrawal-request-view-summary-card">
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested by">
      ${withdrawalRequest.requestedByUser()}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested on">
      ${withdrawalRequest.requestedByDateTime()}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Request reason">
      ${withdrawalRequest.requestText()}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>

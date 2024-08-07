<#include '../../../layout/layout.ftl'>
<#import '../../_caseProcessingActions.ftl' as caseProcessingActions>

<#macro consentBreachSummary consentBreachView consentBreachSummaryCardActions>
  <#assign summaryCardActions>
    <@caseProcessingActions.summaryCardActions actionViews=consentBreachSummaryCardActions/>
  </#assign>
  <@fdsSummaryList.summaryListCard
    headingText="Breach information"
    summaryListId="consent-breach-summary-card"
    cardActionsContent=summaryCardActions>
    <@fdsSummaryList.summaryListRowNoAction keyText="Recorded by">
      ${consentBreachView.addedByUser()}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Recorded on">
     ${consentBreachView.addedDateTime()}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Details of the breach">
      ${consentBreachView.breachText()}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>

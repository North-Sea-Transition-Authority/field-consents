<#include '../../layout/layout.ftl'>
<#import '../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#macro tab paymentsTabPaymentSummaryViews>
  <#if !paymentsTabPaymentSummaryViews?has_content>
    <@fdsInsetText.insetText>
      No payments have been completed yet for this application.
    </@fdsInsetText.insetText>
  <#else>
    <#list paymentsTabPaymentSummaryViews as paymentsTabPaymentSummaryView>
      <@fdsSummaryList.summaryListCard headingText="Payment information" summaryListId="payments-card">
        <@fdsSummaryList.summaryListRowNoAction keyText="Payment status">
          ${paymentsTabPaymentSummaryView.status()}
        </@fdsSummaryList.summaryListRowNoAction>
        <@fdsSummaryList.summaryListRowNoAction keyText="Payment for">
          ${paymentsTabPaymentSummaryView.description()}
        </@fdsSummaryList.summaryListRowNoAction>
        <@fdsSummaryList.summaryListRowNoAction keyText="Payment amount">
          ${paymentsTabPaymentSummaryView.formattedPaymentAmount()}
        </@fdsSummaryList.summaryListRowNoAction>
        <@fdsSummaryList.summaryListRowNoAction keyText="Paid by">
          ${paymentsTabPaymentSummaryView.paidByUser()}
        </@fdsSummaryList.summaryListRowNoAction>
        <@fdsSummaryList.summaryListRowNoAction keyText="Paid on">
          ${paymentsTabPaymentSummaryView.formattedPaymentDate()}
        </@fdsSummaryList.summaryListRowNoAction>
        <@fdsSummaryList.summaryListRowNoAction keyText="GOV.UK Pay reference">
          ${paymentsTabPaymentSummaryView.govUkPayReference()}
        </@fdsSummaryList.summaryListRowNoAction>
      </@fdsSummaryList.summaryListCard>
    </#list>
  </#if>
</#macro>

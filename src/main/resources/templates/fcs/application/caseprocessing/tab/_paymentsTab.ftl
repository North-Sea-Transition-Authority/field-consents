<#include '../../../layout/layout.ftl'>
<#import '../../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#-- @ftlvariable name="serviceBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties" -->

<#macro tab paymentsTabPaymentSummaryViews isMigratedApplication>
  <#local serviceName = serviceBrandingConfigurationProperties.name() />

  <#if !paymentsTabPaymentSummaryViews?has_content>
    <@fdsInsetText.insetText>
      <#if isMigratedApplication>
        Payment completed outside of the new ${serviceName} service.
      <#else>
        No payments have been completed yet for this application.
      </#if>
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

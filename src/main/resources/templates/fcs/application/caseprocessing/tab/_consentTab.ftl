<#include '../../../layout/layout.ftl'>
<#import '../../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>
<#import '../../consent/documents/_consentFilesSummary.ftl' as consentFilesSummary>

<#macro tab showIssuedByUser consentTabConsentSummaryView="">
  <#if !consentTabConsentSummaryView?has_content>
    <@fdsInsetText.insetText>
      A consent has not yet been issued for this application.
    </@fdsInsetText.insetText>
  <#else>
    <@fdsSummaryList.summaryListCard headingText="Consent information" summaryListId="consent-card">
      <#if showIssuedByUser>
        <@fdsSummaryList.summaryListRowNoAction keyText="Issued by">
          ${consentTabConsentSummaryView.issuedByUser()}
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
      <@fdsSummaryList.summaryListRowNoAction keyText="Issued on">
        ${consentTabConsentSummaryView.formattedIssuedDate()}
      </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryListCard>

    <@consentFilesSummary.summary heading="Consent documents" fileViews=consentTabConsentSummaryView.summaryFileViews() />
  </#if>
</#macro>

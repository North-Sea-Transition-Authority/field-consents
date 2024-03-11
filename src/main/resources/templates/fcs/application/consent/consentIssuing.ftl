<#include '../../layout/layout.ftl'>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import 'documents/_consentFilesSummary.ftl' as consentFilesSummary>

<#assign pageTitle = "Consent issuing" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
  backLinkUrl=springUrl(backLinkUrl)
>
  <#if consentIssuingApprovalSummaryView?has_content>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Application ready to grant and issue">
      <@fdsNotificationBanner.notificationBannerContent
        headingText="Application marked as ready to grant and issue"
        moreContent="Marked by ${consentIssuingApprovalSummaryView.formattedApprovedByUser()} on ${consentIssuingApprovalSummaryView.formattedApprovedDate()}"
      />
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>

  <@caseProcessingActions.caseActions actions=actionList />

  <@consentFilesSummary.summary
    heading=consentDocumentsSummaryCard.displayName()
    fileViews=consentDocumentsSummaryCard.summaryData()
    editable=false
  />
</@defaultPage>

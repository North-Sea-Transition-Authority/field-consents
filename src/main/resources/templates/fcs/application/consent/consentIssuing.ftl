<#include '../../layout/layout.ftl'>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import 'documents/_consentFilesSummary.ftl' as consentFilesSummary>
<#import '_approvedForIssue.ftl' as approvedForIssue/>

<#assign pageTitle = "Consent issuing" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
  backLinkUrl=springUrl(backLinkUrl)
  singleErrorMessage=singleErrorMessage
>
  <#if consentIssuingApprovalSummaryView?has_content>
    <@approvedForIssue.notificationBanner consentIssuingApprovalSummaryView=consentIssuingApprovalSummaryView/>
  </#if>

  <@caseProcessingActions.caseActions actions=consentIssuingGroupActionViewList />

  <@consentFilesSummary.summary
    heading=consentDocumentsSummaryCard.displayName()
    fileViews=consentDocumentsSummaryCard.summaryData()
    caseProcessingActionViewList=consentPreparationConsentDocumentsCardGroupActionViewList
  />
</@defaultPage>

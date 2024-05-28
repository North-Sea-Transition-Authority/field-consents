<#include '../../layout/layout.ftl'>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import 'data/_consentDataSummary.ftl' as consentDataSummary>
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

  <@consentDataSummary.summaryCard
    applicationType=applicationType
    consentLengthType=consentLengthType
    consentDataView=consentDataView
    consentFigureUnitView=consentFigureUnitView
  />

  <@consentFilesSummary.summary
    heading=consentDocumentsSummaryCard.displayName()
    fileViews=consentDocumentsSummaryCard.summaryData()
  />
</@defaultPage>

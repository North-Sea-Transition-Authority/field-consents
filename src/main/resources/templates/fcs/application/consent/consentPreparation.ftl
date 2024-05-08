<#include '../../layout/layout.ftl'>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import 'data/_consentDataSummary.ftl' as consentDataSummary>
<#import 'documents/_consentFilesSummary.ftl' as consentFilesSummary>
<#import '_fieldEquityPartner.ftl' as fieldEquityPartner>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
  backLinkUrl=springUrl(backLinkUrl)
  singleErrorMessage=singleErrorMessage
>
  <@caseProcessingActions.caseActions actions=consentPreparationGroupActionViewList />

  <#if consentDataView?has_content>
    <#if fieldEquityPartnersView?has_content>
      <#if fieldEquityPartnersView.organisationGroupNamesWithoutConsentRecipients()?has_content>
        <@fieldEquityPartner.notificationBanner
          fieldEquityPartnersView
          regulatorIndustryAccessManagerRole
          industryAccessManagerRole
          consentRecipientRole
        />
      </#if>
      <@fieldEquityPartner.summaryDetails fieldEquityPartnersView/>
    </#if>
    <@consentDataSummary.summaryCard
      applicationType=applicationType
      consentLengthType=consentLengthType
      consentDataView=consentDataView
      consentFigureUnitView=consentFigureUnitView
      caseProcessingActionViewList=consentPreparationConsentDataCardGroupActionViewList
    />
    <@consentFilesSummary.summary
      heading=consentDocumentsSummaryCard.displayName()
      fileViews=consentDocumentsSummaryCard.summaryData()
      caseProcessingActionViewList=consentPreparationConsentDocumentsCardGroupActionViewList
    />
  <#else>
    <@fdsInsetText.insetText>
      No consent data has been provided yet by the assigned case officer.
    </@fdsInsetText.insetText>
  </#if>
</@defaultPage>

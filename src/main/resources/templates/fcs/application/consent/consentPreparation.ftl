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
>
  <@caseProcessingActions.caseActions actions=actionList />

  <#if fieldEquityPartnersView?has_content>
    <#if fieldEquityPartnersView.organisationGroupNamesWithoutConsentRecipients()?has_content>
      <@fieldEquityPartner.notificationBanner
        fieldEquityPartnersView
        regulatorIndustryAccessManagerRole
        industryAccessManagerRole
        consentRecipientRole
      />
    </#if>
    <@fieldEquityPartner.summaryList fieldEquityPartnersView/>
  </#if>
  <@consentDataSummary.summaryCard
    applicationType=applicationType
    consentLengthType=consentLengthType
    consentDataView=consentDataView
    consentFigureUnitView=consentFigureUnitView
    editUrl=consentDataEditUrl
  />
  <@consentFilesSummary.summary
    heading=consentDocumentsSummaryCard.displayName()
    fileViews=consentDocumentsSummaryCard.summaryData()
    editable=true
    editUrl=consentDocumentsEditUrl
  />
  <@fdsAction.link
    linkText="Save and continue"
    linkClass="govuk-button"
    linkUrl=springUrl(backLinkUrl)
  />
</@defaultPage>

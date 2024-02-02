<#include '../../layout/layout.ftl'>
<#import 'data/_consentDataSummary.ftl' as consentDataSummary>
<#import 'documents/_consentFilesSummary.ftl' as consentFilesSummary>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  backLinkUrl=springUrl(backLinkUrl)>
  <@consentDataSummary.summaryCard
    consentDataView=consentDataView
    editUrl=consentDataEditUrl/>
  <@consentFilesSummary.summary
    heading=consentDocumentsSummaryCard.displayName()
    fileViews=consentDocumentsSummaryCard.summaryData()
    editUrl=consentDocumentsEditUrl/>
  <@fdsAction.link
    linkText="Save and continue"
    linkClass="govuk-button"
    linkUrl=springUrl(backLinkUrl)/>
</@defaultPage>

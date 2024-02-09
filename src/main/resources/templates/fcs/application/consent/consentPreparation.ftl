<#include '../../layout/layout.ftl'>
<#import 'data/_consentDataSummary.ftl' as consentDataSummary>
<#import 'documents/_consentFilesSummary.ftl' as consentFilesSummary>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
  backLinkUrl=springUrl(backLinkUrl)
>
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
    editUrl=consentDocumentsEditUrl
  />
  <#if fieldEquityPartnersView?has_content>
    <@fdsDetails.summaryDetails summaryTitle="Field equity partners">
      <ul class="govuk-list">
        <#list fieldEquityPartnersView.fieldEquityPartnerNames() as fieldEquityPartnerName>
          <li class="govuk-list__item">${fieldEquityPartnerName}</li>
        </#list>
      </ul>
    </@fdsDetails.summaryDetails>
  </#if>
  <@fdsAction.link
    linkText="Save and continue"
    linkClass="govuk-button"
    linkUrl=springUrl(backLinkUrl)
  />
</@defaultPage>

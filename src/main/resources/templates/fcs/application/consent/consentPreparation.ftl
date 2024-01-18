<#include '../../layout/layout.ftl'>
<#import 'data/_consentDataSummary.ftl' as consentDataSummary>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  backLinkUrl=springUrl(backLinkUrl)>
  <@consentDataSummary.summaryCard
    consentDataView=consentDataView
    editUrl=consentDataEditUrl/>
  <@fdsResultList.resultList resultCount=documentInstanceSummaryViews?size resultCountSuffix="document">
    <#list documentInstanceSummaryViews as documentInstanceSummaryView>
      <@fdsResultList.resultListItem
        linkHeadingUrl=springUrl(documentInstanceSummaryView.viewUrl())
        linkHeadingText=documentInstanceSummaryView.title()
        captionHeadingText=documentInstanceSummaryView.description()/>
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>

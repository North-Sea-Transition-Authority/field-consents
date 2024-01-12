<#include '../../layout/layout.ftl'>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
  <@fdsResultList.resultList resultCount=documentInstanceSummaryViews?size resultCountSuffix="document">
    <#list documentInstanceSummaryViews as documentInstanceSummaryView>
      <@fdsResultList.resultListItem
        linkHeadingUrl=springUrl(documentInstanceSummaryView.viewUrl())
        linkHeadingText=documentInstanceSummaryView.title()
        captionHeadingText=documentInstanceSummaryView.description()/>
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>

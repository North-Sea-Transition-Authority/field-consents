<#include '../layout/layout.ftl'>

<#assign pageTitle = "Document templates"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
>
  <@fdsResultList.resultList resultCount=documentTemplateSummaryViews?size resultCountSuffix="document template">
    <#list documentTemplateSummaryViews as documentTemplateSummaryView>
      <@fdsResultList.resultListItem
        captionHeadingText=documentTemplateSummaryView.description()
        linkHeadingUrl=springUrl(documentTemplateSummaryView.viewUrl())
        linkHeadingText=documentTemplateSummaryView.title()
      />
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>

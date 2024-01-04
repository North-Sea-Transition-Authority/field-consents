<#include '../../layout/layout.ftl'>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
  <@fdsResultList.resultList resultCount=documentInstanceViews?size resultCountSuffix="document">
    <#list documentInstanceViews as documentInstanceView>
      <@fdsResultList.resultListItem
        linkHeadingUrl="#"
        linkHeadingText="Heading TODO"
        captionHeadingText="Description TODO"/>
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>

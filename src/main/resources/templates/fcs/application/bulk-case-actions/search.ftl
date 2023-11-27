<#include '../../layout/layout.ftl'>
<#import '../../dataitems/applicationDataItem.ftl' as applicationDataItem>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_PAGE_WIDTH>
  <@fdsSearch.searchPage>
    <@fdsSearch.searchPageContent>
      <@fdsResultList.resultList resultCount=applicationDataItems?size>
        <#list applicationDataItems as dataItem>
          <@applicationDataItem.applicationResultListItem dataItem=dataItem/>
        </#list>
      </@fdsResultList.resultList>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</@defaultPage>

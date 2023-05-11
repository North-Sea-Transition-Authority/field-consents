<#include '../layout/layout.ftl'>

<#function getPageSize wideDisplay>
  <#if wideDisplay>
    <#return PageSize.FULL_PAGE_WIDTH/>
  <#else>
    <#return PageSize.FULL_WIDTH/>
  </#if>
</#function>

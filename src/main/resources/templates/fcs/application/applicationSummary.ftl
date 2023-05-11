<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)
  backLinkUrl=springUrl(backLinkUrl)
>
  <@applicationSummary.applicationSummary accordionId=accordionId/>
</@defaultPage>

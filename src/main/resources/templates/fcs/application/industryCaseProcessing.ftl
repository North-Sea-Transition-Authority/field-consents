<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_caseProcessingActions.ftl' as caseProcessingActions>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=getPageSize.getPageSize(wideSummaryDisplay)
backLinkUrl=springUrl(backLinkUrl)
>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@applicationSummary.applicationSummary accordionId=accordionId/>
</@defaultPage>

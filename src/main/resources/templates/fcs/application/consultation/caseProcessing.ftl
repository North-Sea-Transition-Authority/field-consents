<#include '../../layout/layout.ftl'>
<#import '../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import './consultationRequestInfoBanner.ftl' as consultationRequestInfoBanner>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
    <@consultationRequestInfoBanner.consulationInformationBanner consultationRequestView=consultationRequestView />
    <@caseProcessingActions.caseActions actions=actionList/>
    <@applicationSummary.applicationSummary accordionId=accordionId/>
</@defaultPage>

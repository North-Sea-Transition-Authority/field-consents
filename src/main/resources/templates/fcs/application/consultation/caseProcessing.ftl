<#include '../../layout/layout.ftl'>
<#import '../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import './consultation.ftl' as consulation>
<#import './further-information/furtherInformation.ftl' as furtherInformation>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
    <#if consultationRequestView?has_content>
      <@consulation.notificationBanner consultationRequestView=consultationRequestView />
    </#if>
    <@caseProcessingActions.caseActions actions=actionList/>
    <@applicationSummary.applicationSummary accordionId=accordionId/>
</@defaultPage>

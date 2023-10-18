<#include '../../layout/layout.ftl'>
<#import '../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import './consultation.ftl' as consulation>
<#import '../further-information/requestBanner.ftl' as furtherInformationRequestBanner>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
    <#if furtherInformationRequestView?has_content>
      <@furtherInformationRequestBanner.requestBanner furtherInformationRequestView=furtherInformationRequestView/>
    <#else>
      <@consulation.consulationInformationBanner consultationRequestView=consultationRequestView />
    </#if>
    <@caseProcessingActions.caseActions actions=actionList/>
    <@applicationSummary.applicationSummary accordionId=accordionId/>
</@defaultPage>

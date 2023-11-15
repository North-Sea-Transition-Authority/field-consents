<#include '../../layout/layout.ftl'>
<#include '../caseprocessingtabs/caseProccessingTabs.ftl'>
<#import '../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import '../_applicationContext.ftl' as applicationContextInfo>
<#import './consultation.ftl' as consulation>
<#import './further-information/furtherInformation.ftl' as furtherInformation>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
  <#if consultationRequestView?has_content>
    <@consulation.notificationBanner consultationRequestView=consultationRequestView />
  </#if>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContext/>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@caseProcessingTabsWithContent
    tabs=caseProcessingTabs
    selectedTab=selectedTab
    controllerUrl=controllerUrl>
    <#if selectedTab == "VIEW_APPLICATION">
      <@applicationSummary.applicationSummary accordionId=accordionId/>
    </#if>
    <#if selectedTab == "FURTHER_INFORMATION">
      <@furtherInformation.furtherInformationList furtherInformationViews=furtherInformationViews/>
    </#if>
  </@caseProcessingTabsWithContent>
</@defaultPage>

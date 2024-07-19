<#include '../../layout/layout.ftl'>
<#include '../caseprocessing/tab/caseProccessingTabs.ftl'>
<#import '../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import '../_applicationContext.ftl' as applicationContextInfo>
<#import './consultation.ftl' as consultation>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
  <#if consultationRequestView?has_content>
    <@consultation.notificationBanner consultationRequestView=consultationRequestView/>
  </#if>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContext/>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@caseProcessingTabsWithContent
    tabs=caseProcessingTabs
    selectedTab=selectedTab
    controllerUrl=controllerUrl>
    <#if selectedTab == "VIEW_APPLICATION">
      <@applicationSummary.applicationSummary accordionId=accordionId selectedTab=selectedTab/>
    </#if>
    <#if selectedTab == "CONSULTATIONS">
      <@consultation.consultationList consultationSummaryItems=consultationSummaryItems/>
    </#if>
  </@caseProcessingTabsWithContent>
</@defaultPage>

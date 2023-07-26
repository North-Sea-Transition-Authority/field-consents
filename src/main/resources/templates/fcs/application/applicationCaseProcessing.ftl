<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_caseProcessingActions.ftl' as caseProcessingActions>
<#import './caseprocessingtabs/_caseHistoryTab.ftl' as caseHistoryTab>
<#include '../application/review/reviewDetails.ftl'/>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=getPageSize.getPageSize(wideSummaryDisplay)
backLinkUrl=springUrl(backLinkUrl)
>
  <#if technicalReviewSummaryView??>
    <@reviewDetails technicalReviewSummaryView=technicalReviewSummaryView/>
  </#if>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@fdsBackendTabs.tabs tabsHeading="case processing tabs">
    <@fdsBackendTabs.tabList>
      <#list caseProcessingTabs as tab>
        <@fdsBackendTabs.tab tabLabel=tab.label tabUrl=tab.url.apply(applicationId) tabAnchor=tab.anchor currentTab=selectedTab tabValue=tab.value/>
      </#list>
    </@fdsBackendTabs.tabList>
    <#list caseProcessingTabs as tab>
      <@fdsBackendTabs.tabContent tabAnchor=tab.anchor currentTab=selectedTab tabValue=tab.value>
        <#if tab == "VIEW_APPLICATION">
          <@applicationSummary.applicationSummary accordionId=accordionId/>
        </#if>

        <#if tab == "CASE_HISTORY">
          <@caseHistoryTab.tab caseHistoryEvents=caseHistoryEvents/>
        </#if>
      </@fdsBackendTabs.tabContent>
    </#list>
  </@fdsBackendTabs.tabs>
</@defaultPage>

<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_caseProcessingActions.ftl' as caseProcessingActions>
<#import './caseprocessingtabs/_caseHistoryTab.ftl' as caseHistoryTab>
<#import 'review/technicalReviewDetails.ftl' as technicalReviewDetails/>

<#-- @ftlvariable name="technicalReviewSummaryView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView" -->

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=getPageSize.getPageSize(wideSummaryDisplay)
backLinkUrl=springUrl(backLinkUrl)
>
  <@technicalReviewDetails.notificaitonBanner technicalReviewSummaryView=technicalReviewSummaryView/>
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

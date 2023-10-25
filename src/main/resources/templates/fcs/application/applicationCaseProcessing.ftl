<#include '../layout/layout.ftl'>
<#include '../application/caseprocessingtabs/caseProccessingTabs.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_caseProcessingActions.ftl' as caseProcessingActions>
<#import './caseprocessingtabs/_caseHistoryTab.ftl' as caseHistoryTab>
<#import 'review/technicalReviewDetails.ftl' as technicalReviewDetails/>
<#import '../application/consultation/further-information/furtherInformation.ftl' as furtherInformation/>

<#-- @ftlvariable name="technicalReviewSummaryView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
  <#if technicalReviewSummaryView?has_content>
    <@technicalReviewDetails.notificationBanner technicalReviewSummaryView=technicalReviewSummaryView/>
  </#if>
  <#if furtherInformationView?has_content>
    <@furtherInformation.requestNotificationBanner furtherInformationView=furtherInformationView/>
  </#if>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@caseProcessingTabsWithContent
    tabs=caseProcessingTabs
    selectedTab=selectedTab
    controllerUrl=controllerUrl>
    <#if selectedTab == "VIEW_APPLICATION">
      <@applicationSummary.applicationSummary accordionId=accordionId/>
    </#if>
    <#if selectedTab == "CASE_HISTORY">
      <@caseHistoryTab.tab caseHistoryEvents=caseHistoryEvents/>
    </#if>
  </@caseProcessingTabsWithContent>
</@defaultPage>

<#include '../layout/layout.ftl'>
<#include '../application/caseprocessing/tab/caseProccessingTabs.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_caseProcessingActions.ftl' as caseProcessingActions>
<#import './_applicationContext.ftl' as applicationContextInfo>
<#import './caseprocessing/tab/_caseHistoryTab.ftl' as caseHistoryTab>
<#import 'review/technicalReviewDetails.ftl' as technicalReviewDetails/>
<#import '../application/consultation/further-information/furtherInformation.ftl' as furtherInformation/>
<#import './caseprocessing/tab/_paymentsTab.ftl' as paymentsTab>
<#import './caseprocessing/tab/_consentTab.ftl' as consentTab>
<#import 'consent/_approvedForIssue.ftl' as approvedForIssue/>

<#-- @ftlvariable name="technicalReviewSummaryView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView" -->
<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.fieldconsents.tasklist.TaskListSection>" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
  <#if consentIssuingApprovalSummaryView?has_content>
    <@approvedForIssue.notificationBanner consentIssuingApprovalSummaryView=consentIssuingApprovalSummaryView/>
  </#if>
  <#if technicalReviewSummaryView?has_content>
    <@technicalReviewDetails.notificationBanner technicalReviewSummaryView=technicalReviewSummaryView/>
  </#if>
  <#if furtherInformationView?has_content>
    <@furtherInformation.requestNotificationBanner furtherInformationView=furtherInformationView/>
  </#if>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContext/>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@caseProcessingTabsWithContent
    tabs=caseProcessingTabs
    selectedTab=selectedTab
    controllerUrl=controllerUrl>
    <#if selectedTab == "TASKS">
      <@taskList.standardTaskList taskListSections=taskListSections showSectionNumber=false/>
    </#if>
    <#if selectedTab == "VIEW_APPLICATION">
      <@applicationSummary.applicationSummary accordionId=accordionId/>
    </#if>
    <#if selectedTab == "CASE_HISTORY">
      <@caseHistoryTab.tab caseHistoryEvents=caseHistoryEvents/>
    </#if>
    <#if selectedTab == "PAYMENTS">
      <@paymentsTab.tab paymentsTabPaymentSummaryViews=paymentsTabPaymentSummaryViews/>
    </#if>
    <#if selectedTab == "CONSENT">
      <@consentTab.tab showIssuedByUser=true consentTabConsentSummaryView=consentTabConsentSummaryView/>
    </#if>
  </@caseProcessingTabsWithContent>
</@defaultPage>

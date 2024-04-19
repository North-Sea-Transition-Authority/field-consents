<#include '../layout/layout.ftl'>
<#include '../application/caseprocessing/tab/caseProccessingTabs.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_caseProcessingActions.ftl' as caseProcessingActions>
<#import './_applicationContext.ftl' as applicationContextInfo>
<#import 'update/_applicationUpdateRequestBanner.ftl' as applicationUpdateRequestBanner>
<#import './caseprocessing/tab/_paymentsTab.ftl' as paymentsTab>
<#import './caseprocessing/tab/_consentTab.ftl' as consentTab>

<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestView" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
  <@applicationUpdateRequestBanner.applicationUpdateRequestBanner applicationUpdateRequestView=applicationUpdateRequestView!""/>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContext/>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@caseProcessingTabsWithContent
    tabs=caseProcessingTabs
    selectedTab=selectedTab
    controllerUrl=controllerUrl>
    <#if selectedTab == "VIEW_APPLICATION">
      <@applicationSummary.applicationSummary accordionId=accordionId/>
    </#if>
    <#if selectedTab == "PAYMENTS">
      <@paymentsTab.tab paymentsTabPaymentSummaryViews=paymentsTabPaymentSummaryViews isMigratedApplication=isMigratedApplication/>
    </#if>
    <#if selectedTab == "CONSENT">
      <@consentTab.tab showIssuedByUser=false consentTabConsentSummaryView=consentTabConsentSummaryView/>
    </#if>
  </@caseProcessingTabsWithContent>
</@defaultPage>
